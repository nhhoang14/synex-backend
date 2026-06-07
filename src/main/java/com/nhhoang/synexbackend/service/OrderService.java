package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.CreateOrderRequest;
import com.nhhoang.synexbackend.dto.response.OrderItemResponse;
import com.nhhoang.synexbackend.dto.response.OrderResponse;
import com.nhhoang.synexbackend.entity.*;
import com.nhhoang.synexbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }

        User currentUser = getCurrentUser();
        ShippingAddress shippingAddress = resolveShippingAddress(currentUser, request.getShippingAddressId());

        List<CartItem> cartItems = cartItemRepository.findAllByUserId(currentUser.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<CartItem> selectedItems = resolveSelectedCartItems(cartItems, request.getSelectedCartItemIds());
        validateStockAvailability(selectedItems);

        Order order = new Order();
        order.setUser(currentUser);
        copyShippingSnapshot(order, shippingAddress);
        order.setStatus("PENDING");
        order.setPaymentMethod(request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()
                ? "COD"
                : request.getPaymentMethod().trim());

        double totalAmount = 0.0;
        List<OrderItem> savedItems = new ArrayList<>();

        order = orderRepository.save(order);

        for (CartItem cartItem : selectedItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ProductVariant variant = resolveOrderVariant(product, cartItem.getVariant());

            double unitPrice = variant.getPrice();
            int availableStock = variant.getStockQuantity();

            if (availableStock < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(unitPrice);

            totalAmount += unitPrice * cartItem.getQuantity();
            savedItems.add(orderItemRepository.save(orderItem));
        }

        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);
        order.setOrderCode("DH" + order.getId());
        order.setItems(savedItems);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(order.getPaymentMethod());
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        cartItemRepository.deleteAll(selectedItems);

        return mapToResponse(order);
    }

    public void validateSelectedItemsStock(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }

        User currentUser = getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findAllByUserId(currentUser.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<CartItem> selectedItems = resolveSelectedCartItems(cartItems, request.getSelectedCartItemIds());
        validateStockAvailability(selectedItems);
    }

    private List<CartItem> resolveSelectedCartItems(List<CartItem> cartItems, List<Long> selectedCartItemIds) {
        if (selectedCartItemIds == null || selectedCartItemIds.isEmpty()) {
            return cartItems;
        }

        Set<Long> selectedIds = selectedCartItemIds.stream()
                .collect(Collectors.toSet());

        List<CartItem> selectedItems = cartItems.stream()
                .filter(item -> item.getId() != null && selectedIds.contains(item.getId()))
                .toList();

        if (selectedItems.isEmpty()) {
            throw new RuntimeException("No valid cart items were selected");
        }

        if (selectedItems.size() != selectedIds.size()) {
            throw new RuntimeException("Some selected cart items do not belong to the current cart");
        }

        return selectedItems;
    }

    private void validateStockAvailability(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ProductVariant variant = resolveOrderVariant(product, cartItem.getVariant());
            int availableStock = variant.getStockQuantity();

            if (availableStock < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
        }
    }

    private ShippingAddress resolveShippingAddress(User currentUser, Long requestedAddressId) {
        if (requestedAddressId != null) {
            ShippingAddress selectedAddress = shippingAddressRepository.findById(requestedAddressId)
                    .orElseThrow(() -> new RuntimeException("Shipping address not found"));

            if (!selectedAddress.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized shipping address");
            }

            return selectedAddress;
        }

        return shippingAddressRepository.findFirstByUserIdAndIsDefaultTrue(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("No default shipping address. Please choose or create one"));
    }

    public List<OrderResponse> getCurrentUserOrders() {
        User currentUser = getCurrentUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getCurrentUserOrder(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return mapToResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    public OrderResponse updateOrderStatus(Long orderId, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status.trim().toUpperCase());
        return mapToResponse(orderRepository.save(order));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = (order.getItems() == null)
                ? Collections.emptyList()
                : order.getItems().stream()
                    .map(item -> new OrderItemResponse(
                            item.getProduct().getId(),
                            buildOrderItemName(item),
                            item.getQuantity(),
                            item.getPrice(),
                            item.getPrice() * item.getQuantity()
                    ))
                    .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser() == null ? null : order.getUser().getId(),
                order.getTotalAmount(),
                order.getOrderCode(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getShippingFullName(),
                order.getShippingPhone(),
                order.getShippingStreet(),
                order.getShippingWard(),
                order.getShippingProvince(),
                order.getShippingNotes(),
                itemResponses
        );
    }

    private void copyShippingSnapshot(Order order, ShippingAddress shippingAddress) {
        order.setShippingFullName(shippingAddress.getFullName());
        order.setShippingPhone(shippingAddress.getPhone());
        order.setShippingStreet(shippingAddress.getStreet());
        order.setShippingWard(shippingAddress.getWard());
        order.setShippingProvince(shippingAddress.getProvince());
        order.setShippingNotes(null);
    }

    private ProductVariant resolveOrderVariant(Product product, ProductVariant variantFromCart) {
        if (variantFromCart == null) {
            List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
            if (variants.size() == 1) {
                ProductVariant onlyVariant = variants.get(0);
                if (!onlyVariant.isActive()) {
                    throw new RuntimeException("Selected variant is inactive");
                }

                return onlyVariant;
            }

            throw new RuntimeException("Please select a variant for this product");
        }

        ProductVariant variant = productVariantRepository.findById(variantFromCart.getId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (!variant.getProduct().getId().equals(product.getId())) {
            throw new RuntimeException("Variant does not belong to selected product");
        }

        if (!variant.isActive()) {
            throw new RuntimeException("Selected variant is inactive");
        }

        return variant;
    }

    private String buildOrderItemName(OrderItem item) {
        if (item.getVariant() == null) {
            return item.getProduct().getName();
        }

        String sku = item.getVariant().getSku();
        if (sku == null || sku.isBlank()) {
            return item.getProduct().getName();
        }

        return item.getProduct().getName() + " [" + sku + "]";
    }

    @Transactional
    public void processOrderPaymentViaWebhook(String sepayContent, Double amount) {
        String orderCode = sepayContent.trim().toUpperCase();

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + orderCode));

        if (!"PENDING".equals(order.getStatus())) {
            System.out.println("-> [SePay] Đơn hàng " + orderCode + " đang ở trạng thái [" + order.getStatus() + "], bỏ qua không xử lý.");
            return;
        }

        if (Math.abs(order.getTotalAmount() - amount) < 1.0) {
            order.setStatus("SHIPPING"); 
            orderRepository.save(order);
            
            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Payment cho đơn hàng ID: " + order.getId()));
            
            payment.setStatus("COMPLETED");
            paymentRepository.save(payment);

            System.out.println("-> [SePay Webhook] Đơn hàng " + orderCode + " đã trả đủ tiền. Tự động chuyển sang ĐANG GIAO!");
        } else {
            System.err.println("-> [SePay Webhook] Sai tiền! Đơn " + orderCode + " cần " + order.getTotalAmount() + " nhưng nhận " + amount);
            throw new RuntimeException("Payment amount mismatch");
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredCardOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(20);
        List<Order> expiredOrders = orderRepository.findByPaymentMethodAndStatusAndCreatedAtBefore("CARD", "PENDING", threshold);

        for (Order order : expiredOrders) {
            order.setStatus("CANCELLED");
            
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    ProductVariant variant = item.getVariant();
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                    productVariantRepository.save(variant);
                }
            }
            orderRepository.save(order);
            
            paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                payment.setStatus("CANCELLED");
                paymentRepository.save(payment);
            });

            System.out.println("-> [Hệ thống] Đã hủy đơn hàng quá hạn thanh toán CARD: " + order.getOrderCode());
        }
    }
}