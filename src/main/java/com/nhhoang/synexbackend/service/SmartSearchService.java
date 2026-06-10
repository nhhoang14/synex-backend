// package com.nhhoang.synexbackend.service;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.nhhoang.synexbackend.dto.response.SearchIntent;
// import com.nhhoang.synexbackend.entity.Product;
// import com.nhhoang.synexbackend.entity.ProductVariant;
// import com.nhhoang.synexbackend.entity.ProductVariantAttribute;
// import com.nhhoang.synexbackend.repository.ProductRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.StringUtils;
// import org.springframework.web.client.RestTemplate;

// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Locale;
// import java.util.Map;
// import java.util.stream.Collectors;

// @Service
// @Slf4j
// @RequiredArgsConstructor
// public class SmartSearchService {

//     private final ProductRepository productRepository;
//     private final LlmParserService llmParserService;
//     private final EmbeddingService embeddingService;
//     private final RestTemplate restTemplate;
//     private final ObjectMapper objectMapper;

//     @Value("${app.search.elasticsearch-url:}")
//     private String elasticsearchUrl;

//     @Value("${app.search.elasticsearch-index:products}")
//     private String elasticsearchIndex;

//     @Transactional(readOnly = true)
//     public List<Product> search(String query) {
//         SearchIntent intent = llmParserService.parseQuery(query);
//         float[] queryVector = embeddingService.getEmbedding(query);

//         List<Product> elasticResults = searchWithElasticsearch(query, intent, queryVector);
//         if (!elasticResults.isEmpty()) {
//             return elasticResults;
//         }

//         return searchInDatabase(query, intent);
//     }

//     private List<Product> searchWithElasticsearch(String query, SearchIntent intent, float[] queryVector) {
//         if (!StringUtils.hasText(elasticsearchUrl)) {
//             return List.of();
//         }

//         try {
//             Map<String, Object> requestBody = buildSearchRequest(query, intent, queryVector);
//             String response = restTemplate.postForObject(elasticsearchSearchUrl(), requestBody, String.class);
//             if (!StringUtils.hasText(response)) {
//                 return List.of();
//             }

//             Map<String, Object> responseMap = objectMapper.readValue(response, new TypeReference<>() {
//             });
//             return extractProductsFromElasticResponse(responseMap);
//         } catch (Exception ex) {
//             log.debug("Elasticsearch search unavailable, falling back to database search: {}", ex.getMessage());
//             return List.of();
//         }
//     }

//     private Map<String, Object> buildSearchRequest(String query, SearchIntent intent, float[] queryVector) {
//         Map<String, Object> request = new LinkedHashMap<>();
//         request.put("size", 20);

//         Map<String, Object> boolQuery = new LinkedHashMap<>();
//         List<Map<String, Object>> filters = new ArrayList<>();
//         List<Map<String, Object>> shouldClauses = new ArrayList<>();

//         filters.add(termFilter("active", true));

//         if (StringUtils.hasText(intent.getCategory())) {
//             shouldClauses.add(matchClause("category.name", intent.getCategory()));
//         }

//         if (intent.getMinPrice() != null || intent.getMaxPrice() != null) {
//             Map<String, Object> range = new LinkedHashMap<>();
//             if (intent.getMinPrice() != null) {
//                 range.put("gte", intent.getMinPrice());
//             }
//             if (intent.getMaxPrice() != null) {
//                 range.put("lte", intent.getMaxPrice());
//             }
//             filters.add(Map.of("range", Map.of("variants.price", range)));
//         }

//         if (StringUtils.hasText(query)) {
//             shouldClauses.add(matchClause("name", query));
//             shouldClauses.add(matchClause("description", query));
//             shouldClauses.add(matchClause("brand.name", query));
//             shouldClauses.add(matchClause("category.name", query));
//         }

//         if (!shouldClauses.isEmpty()) {
//             boolQuery.put("should", shouldClauses);
//             boolQuery.put("minimum_should_match", 1);
//         }

//         if (!filters.isEmpty()) {
//             boolQuery.put("filter", filters);
//         }

//         request.put("query", Map.of("bool", boolQuery));

//         if (queryVector != null && queryVector.length > 0) {
//             request.put("knn", Map.of(
//                     "field", "embedding",
//                     "query_vector", toList(queryVector),
//                     "k", 20,
//                     "num_candidates", 100
//             ));
//         }

//         return request;
//     }

//     private List<Product> extractProductsFromElasticResponse(Map<String, Object> responseMap) {
//         Object hitsObject = responseMap.get("hits");
//         if (!(hitsObject instanceof Map<?, ?> hitsMap)) {
//             return List.of();
//         }

//         Object nestedHitsObject = hitsMap.get("hits");
//         if (!(nestedHitsObject instanceof List<?> nestedHits)) {
//             return List.of();
//         }

//         List<Product> products = new ArrayList<>();
//         for (Object hitObject : nestedHits) {
//             if (!(hitObject instanceof Map<?, ?> hitMap)) {
//                 continue;
//             }

//             Object sourceObject = hitMap.get("_source");
//             if (!(sourceObject instanceof Map<?, ?> sourceMap)) {
//                 continue;
//             }

//             products.add(objectMapper.convertValue(sourceMap, Product.class));
//         }

//         return products;
//     }

//     private List<Product> searchInDatabase(String query, SearchIntent intent) {
//         List<Product> products = productRepository.findByActiveTrue();
//         if (products.isEmpty()) {
//             return List.of();
//         }

//         String normalizedQuery = normalize(query);
//         List<ScoredProduct> scoredProducts = new ArrayList<>();
//         for (Product product : products) {
//             double score = scoreProduct(product, normalizedQuery, intent);
//             if (score >= 0) {
//                 scoredProducts.add(new ScoredProduct(product, score, representativePrice(product), product.getId() == null ? Long.MAX_VALUE : product.getId()));
//             }
//         }

//         Comparator<ScoredProduct> comparator = Comparator.comparingDouble(ScoredProduct::score).reversed()
//                 .thenComparing(ScoredProduct::productName);

//         if (StringUtils.hasText(intent.getSortField()) && "price".equalsIgnoreCase(intent.getSortField())) {
//             Comparator<ScoredProduct> priceComparator = Comparator.comparingDouble(ScoredProduct::price);
//             if ("desc".equalsIgnoreCase(intent.getSortOrder())) {
//                 priceComparator = priceComparator.reversed();
//             }
//             comparator = priceComparator.thenComparing(comparator);
//         } else if (StringUtils.hasText(intent.getSortField()) && "createdAt".equalsIgnoreCase(intent.getSortField())) {
//             Comparator<ScoredProduct> idComparator = Comparator.comparingLong(ScoredProduct::sortId);
//             if ("desc".equalsIgnoreCase(intent.getSortOrder())) {
//                 idComparator = idComparator.reversed();
//             }
//             comparator = idComparator.thenComparing(comparator);
//         }

//         return scoredProducts.stream()
//                 .sorted(comparator)
//                 .limit(20)
//                 .map(ScoredProduct::product)
//                 .collect(Collectors.toList());
//     }

//     private double scoreProduct(Product product, String normalizedQuery, SearchIntent intent) {
//         if (product == null) {
//             return -1;
//         }

//         if (!matchesCategory(product, intent) || !matchesPrice(product, intent) || !matchesColors(product, intent) || !matchesAttributes(product, intent)) {
//             return -1;
//         }

//         if (!StringUtils.hasText(normalizedQuery) && !hasIntentFilters(intent)) {
//             return 0;
//         }

//         String searchableText = buildSearchableText(product);
//         if (!StringUtils.hasText(normalizedQuery)) {
//             return 10;
//         }

//         double score = 0;
//         for (String token : normalizedQuery.split("\\s+")) {
//             if (token.length() < 2) {
//                 continue;
//             }

//             if (searchableText.contains(token)) {
//                 score += 2;
//             }
//         }

//         if (searchableText.contains(normalizedQuery)) {
//             score += 8;
//         }

//         if (product.getBrand() != null && StringUtils.hasText(product.getBrand().getName())
//                 && searchableText.contains(normalize(product.getBrand().getName()))) {
//             score += 4;
//         }

//         if (product.getCategory() != null && StringUtils.hasText(product.getCategory().getName())
//                 && searchableText.contains(normalize(product.getCategory().getName()))) {
//             score += 4;
//         }

//         return score;
//     }

//     private boolean matchesCategory(Product product, SearchIntent intent) {
//         if (!StringUtils.hasText(intent.getCategory())) {
//             return true;
//         }

//         if (product.getCategory() == null || !StringUtils.hasText(product.getCategory().getName())) {
//             return false;
//         }

//         String productCategory = normalize(product.getCategory().getName());
//         String expectedCategory = normalize(intent.getCategory());
//         return productCategory.contains(expectedCategory) || expectedCategory.contains(productCategory);
//     }

//     private boolean matchesPrice(Product product, SearchIntent intent) {
//         if (intent.getMinPrice() == null && intent.getMaxPrice() == null) {
//             return true;
//         }

//         Double price = representativePrice(product);
//         if (price == null) {
//             return false;
//         }

//         if (intent.getMinPrice() != null && price < intent.getMinPrice()) {
//             return false;
//         }

//         return intent.getMaxPrice() == null || price <= intent.getMaxPrice();
//     }

//     private boolean matchesColors(Product product, SearchIntent intent) {
//         if (intent.getColors() == null || intent.getColors().isEmpty()) {
//             return true;
//         }

//         String searchableText = buildSearchableText(product);
//         for (String color : intent.getColors()) {
//             if (StringUtils.hasText(color) && searchableText.contains(normalize(color))) {
//                 return true;
//             }
//         }

//         return false;
//     }

//     private boolean matchesAttributes(Product product, SearchIntent intent) {
//         if (intent.getAttributes() == null || intent.getAttributes().isEmpty()) {
//             return true;
//         }

//         String searchableText = buildSearchableText(product);
//         for (String attribute : intent.getAttributes()) {
//             if (StringUtils.hasText(attribute) && searchableText.contains(normalize(attribute))) {
//                 return true;
//             }
//         }

//         return false;
//     }

//     private boolean hasIntentFilters(SearchIntent intent) {
//         return StringUtils.hasText(intent.getCategory())
//                 || intent.getMinPrice() != null
//                 || intent.getMaxPrice() != null
//                 || (intent.getColors() != null && !intent.getColors().isEmpty())
//                 || (intent.getAttributes() != null && !intent.getAttributes().isEmpty());
//     }

//     private String buildSearchableText(Product product) {
//         List<String> parts = new ArrayList<>();
//         addText(parts, product.getName());
//         addText(parts, product.getDescription());

//         if (product.getCategory() != null) {
//             addText(parts, product.getCategory().getName());
//         }

//         if (product.getBrand() != null) {
//             addText(parts, product.getBrand().getName());
//         }

//         if (product.getVariants() != null) {
//             for (ProductVariant variant : product.getVariants()) {
//                 addText(parts, variant.getSku());
//                 addText(parts, variant.getImageUrl());

//                 if (variant.getAttributes() != null) {
//                     for (ProductVariantAttribute attribute : variant.getAttributes()) {
//                         addText(parts, attribute.getAttributeName());
//                         addText(parts, attribute.getAttributeValue());
//                     }
//                 }
//             }
//         }

//         return String.join(" ", parts);
//     }

//     private void addText(List<String> parts, String value) {
//         if (StringUtils.hasText(value)) {
//             parts.add(normalize(value));
//         }
//     }

//     private String normalize(String value) {
//         if (value == null) {
//             return "";
//         }

//         String lower = value.toLowerCase(Locale.ROOT).trim();
//         return java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
//                 .replaceAll("\\p{M}+", "")
//                 .replaceAll("\\s+", " ")
//                 .trim();
//     }

//     private Double representativePrice(Product product) {
//         if (product.getVariants() == null || product.getVariants().isEmpty()) {
//             return null;
//         }

//         return product.getVariants().stream()
//                 .filter(variant -> variant != null && variant.isActive())
//                 .map(ProductVariant::getPrice)
//                 .min(Double::compareTo)
//                 .orElse(null);
//     }

//     private Map<String, Object> termFilter(String field, Object value) {
//         return Map.of("term", Map.of(field, value));
//     }

//     private Map<String, Object> matchClause(String field, String query) {
//         return Map.of("match", Map.of(field, Map.of("query", query, "operator", "and")));
//     }

//     private List<Float> toList(float[] vector) {
//         List<Float> values = new ArrayList<>(vector.length);
//         for (float value : vector) {
//             values.add(value);
//         }
//         return values;
//     }

//     private String elasticsearchSearchUrl() {
//         if (elasticsearchUrl.endsWith("/")) {
//             return elasticsearchUrl + elasticsearchIndex + "/_search";
//         }
//         return elasticsearchUrl + "/" + elasticsearchIndex + "/_search";
//     }

//     private record ScoredProduct(Product product, double score, double price, long sortId) {
//         private String productName() {
//             return product != null && StringUtils.hasText(product.getName())
//                     ? product.getName().toLowerCase(Locale.ROOT)
//                     : "";
//         }
//     }
// }