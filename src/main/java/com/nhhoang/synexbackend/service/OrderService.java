package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.CreateOrderRequest;
import com.nhhoang.synexbackend.dto.response.OrderItemResponse;
import com.nhhoang.synexbackend.dto.response.OrderResponse;
import com.nhhoang.synexbackend.entity.*;
import com.nhhoang.synexbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
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

        Cart cart = cartRepository.findByUserId(currentUser.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(currentUser);
        order.setShippingAddress(shippingAddress);
        order.setStatus("PENDING");
        order.setPaymentMethod(request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()
                ? "COD"
                : request.getPaymentMethod().trim());

        double totalAmount = 0.0;
        List<OrderItem> savedItems = new ArrayList<>();

        order = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ProductVariant variant = resolveOrderVariant(product, cartItem.getVariant());

            double unitPrice = variant != null ? variant.getPrice() : product.getPrice();
            int availableStock = variant != null ? variant.getStockQuantity() : product.getStockQuantity();

            if (availableStock < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            if (variant != null) {
                variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
                productVariantRepository.save(variant);
            } else {
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }

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
        order.setItems(savedItems);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(order.getPaymentMethod());
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        cartItemRepository.deleteByCartId(cart.getId());

        return mapToResponse(order, savedItems);
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
        return mapToResponse(order, order.getItems() == null ? Collections.emptyList() : order.getItems());
    }

    private OrderResponse mapToResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
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
                order.getShippingAddress() == null ? null : order.getShippingAddress().getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    private ProductVariant resolveOrderVariant(Product product, ProductVariant variantFromCart) {
        if (variantFromCart == null) {
            return null;
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
}