// package com.nhhoang.synexbackend.service;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.nhhoang.synexbackend.dto.response.SearchIntent;
// import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.util.StringUtils;
// import org.springframework.web.client.RestTemplate;

// import java.text.Normalizer;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Locale;
// import java.util.Map;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// @Service
// @RequiredArgsConstructor
// public class LlmParserService {
//     private static final List<String> COLORS = List.of(
//             "đen", "trắng", "xanh", "đỏ", "vàng", "cam", "hồng", "tím", "nâu", "xám", "bạc", "be",
//             "black", "white", "blue", "red", "green", "yellow", "pink", "purple", "brown", "gray", "silver", "gold"
//     );

//     private static final Map<String, String> CATEGORY_KEYWORDS = Map.ofEntries(
//             Map.entry("giày", "giày"),
//             Map.entry("sneaker", "giày"),
//             Map.entry("áo khoác", "áo khoác"),
//             Map.entry("áo", "áo"),
//             Map.entry("quần", "quần"),
//             Map.entry("quan", "quần"),
//             Map.entry("váy", "váy"),
//             Map.entry("đầm", "váy"),
//             Map.entry("túi", "túi"),
//             Map.entry("đồng hồ", "đồng hồ"),
//             Map.entry("mũ", "mũ"),
//             Map.entry("nón", "mũ"),
//             Map.entry("balo", "balo")
//     );

//     private final ObjectMapper objectMapper;
//     private final RestTemplate restTemplate;

//     @Value("${app.llm.parser.url:}")
//     private String llmParserUrl;

//     public SearchIntent parseQuery(String query) {
//         SearchIntent fallbackIntent = parseWithRules(query);
//         if (!StringUtils.hasText(llmParserUrl) || !StringUtils.hasText(query)) {
//             return fallbackIntent;
//         }

//         try {
//             String response = restTemplate.postForObject(llmParserUrl, Map.of(
//                     "query", query,
//                     "prompt", buildPrompt(query)
//             ), String.class);

//             if (!StringUtils.hasText(response)) {
//                 return fallbackIntent;
//             }

//             SearchIntent parsedIntent = objectMapper.readValue(response, SearchIntent.class);
//             return mergeIntent(fallbackIntent, parsedIntent);
//         } catch (Exception ignored) {
//             return fallbackIntent;
//         }
//     }

//     private SearchIntent parseWithRules(String query) {
//         SearchIntent intent = new SearchIntent();
//         if (!StringUtils.hasText(query)) {
//             return intent;
//         }

//         String normalized = normalize(query);
//         intent.setCategory(detectCategory(normalized));
//         applyPriceRules(normalized, intent);
//         intent.setColors(extractColors(normalized));
//         intent.setAttributes(extractAttributes(normalized));
//         applySortRules(normalized, intent);
//         return intent;
//     }

//     private String buildPrompt(String query) {
//         return "Analyze the following e-commerce search query: \"%s\". Extract JSON with fields category, minPrice, maxPrice, colors, attributes, sortField, sortOrder. Return only raw JSON.".formatted(query);
//     }

//     private SearchIntent mergeIntent(SearchIntent fallback, SearchIntent parsed) {
//         SearchIntent merged = new SearchIntent();
//         merged.setCategory(StringUtils.hasText(parsed.getCategory()) ? parsed.getCategory() : fallback.getCategory());
//         merged.setMinPrice(parsed.getMinPrice() != null ? parsed.getMinPrice() : fallback.getMinPrice());
//         merged.setMaxPrice(parsed.getMaxPrice() != null ? parsed.getMaxPrice() : fallback.getMaxPrice());
//         merged.setColors(parsed.getColors() != null && !parsed.getColors().isEmpty() ? parsed.getColors() : fallback.getColors());
//         merged.setAttributes(parsed.getAttributes() != null && !parsed.getAttributes().isEmpty() ? parsed.getAttributes() : fallback.getAttributes());
//         merged.setSortField(StringUtils.hasText(parsed.getSortField()) ? parsed.getSortField() : fallback.getSortField());
//         merged.setSortOrder(StringUtils.hasText(parsed.getSortOrder()) ? parsed.getSortOrder() : fallback.getSortOrder());
//         return merged;
//     }

//     private String detectCategory(String normalizedQuery) {
//         for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
//             if (normalizedQuery.contains(entry.getKey())) {
//                 return entry.getValue();
//             }
//         }
//         return null;
//     }

//     private void applyPriceRules(String normalizedQuery, SearchIntent intent) {
//         Matcher rangeMatcher = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(k|nghìn|ngàn|triệu|tr|đ|d)?\\s*(?:đến|-|~|tới)\\s*(\\d+(?:[.,]\\d+)?)\\s*(k|nghìn|ngàn|triệu|tr|đ|d)?").matcher(normalizedQuery);
//         if (rangeMatcher.find()) {
//             intent.setMinPrice(parseMoney(rangeMatcher.group(1), rangeMatcher.group(2)));
//             intent.setMaxPrice(parseMoney(rangeMatcher.group(3), rangeMatcher.group(4)));
//             return;
//         }

//         Matcher underMatcher = Pattern.compile("(?:dưới|<=|không quá|tối đa|max)\\s*(\\d+(?:[.,]\\d+)?)\\s*(k|nghìn|ngàn|triệu|tr|đ|d)?").matcher(normalizedQuery);
//         if (underMatcher.find()) {
//             intent.setMaxPrice(parseMoney(underMatcher.group(1), underMatcher.group(2)));
//         }

//         Matcher overMatcher = Pattern.compile("(?:trên|>=|ít nhất|từ|hơn)\\s*(\\d+(?:[.,]\\d+)?)\\s*(k|nghìn|ngàn|triệu|tr|đ|d)?").matcher(normalizedQuery);
//         if (overMatcher.find()) {
//             intent.setMinPrice(parseMoney(overMatcher.group(1), overMatcher.group(2)));
//         }
//     }

//     private Double parseMoney(String amount, String unit) {
//         if (!StringUtils.hasText(amount)) {
//             return null;
//         }

//         double value = Double.parseDouble(amount.replace(',', '.'));
//         if (!StringUtils.hasText(unit)) {
//             return value;
//         }

//         return switch (unit) {
//             case "k", "nghìn", "ngàn" -> value * 1_000d;
//             case "triệu", "tr" -> value * 1_000_000d;
//             default -> value;
//         };
//     }

//     private List<String> extractColors(String normalizedQuery) {
//         List<String> colors = new ArrayList<>();
//         for (String color : COLORS) {
//             if (normalizedQuery.contains(color)) {
//                 colors.add(color);
//             }
//         }
//         return colors;
//     }

//     private List<String> extractAttributes(String normalizedQuery) {
//         List<String> attributes = new ArrayList<>();
//         Matcher sizeMatcher = Pattern.compile("\\bsize\\s*([xsml]{1,4}|\\d{2,3})\\b").matcher(normalizedQuery);
//         if (sizeMatcher.find()) {
//             attributes.add("size:" + sizeMatcher.group(1));
//         }
//         return attributes;
//     }

//     private void applySortRules(String normalizedQuery, SearchIntent intent) {
//         if (normalizedQuery.contains("rẻ nhất") || normalizedQuery.contains("thấp nhất") || normalizedQuery.contains("giá thấp")) {
//             intent.setSortField("price");
//             intent.setSortOrder("asc");
//             return;
//         }

//         if (normalizedQuery.contains("đắt nhất") || normalizedQuery.contains("cao nhất") || normalizedQuery.contains("giá cao")) {
//             intent.setSortField("price");
//             intent.setSortOrder("desc");
//             return;
//         }

//         if (normalizedQuery.contains("mới nhất") || normalizedQuery.contains("latest") || normalizedQuery.contains("gần đây")) {
//             intent.setSortField("createdAt");
//             intent.setSortOrder("desc");
//             return;
//         }

//         if (normalizedQuery.contains("cũ nhất")) {
//             intent.setSortField("createdAt");
//             intent.setSortOrder("asc");
//         }
//     }

//     private String normalize(String value) {
//         String lower = value.toLowerCase(Locale.ROOT).trim();
//         String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
//         return decomposed.replaceAll("\\p{M}+", "").replaceAll("\\s+", " ").trim();
//     }
// }