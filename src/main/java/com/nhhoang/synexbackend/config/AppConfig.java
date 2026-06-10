// package com.nhhoang.synexbackend.config;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.client.RestTemplate;

// @Configuration
// public class AppConfig {

//     @Bean
//     public ObjectMapper objectMapper() {
//         ObjectMapper mapper = new ObjectMapper();
//         // Đăng ký JavaTimeModule để xử lý các kiểu dữ liệu LocalDateTime trong Entity của bạn
//         mapper.registerModule(new JavaTimeModule());
//         return mapper;
//     }

//     @Bean
//     public RestTemplate restTemplate() {
//         return new RestTemplate();
//     }
// }