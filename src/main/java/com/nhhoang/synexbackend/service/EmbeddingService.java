// package com.nhhoang.synexbackend.service;

// import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// @Service
// @RequiredArgsConstructor
// public class EmbeddingService {
//     private final RestTemplate restTemplate;
//     @Value("${app.embedding.url:http://localhost:8000/embed}")
//     private String bgeServerUrl;

//     public float[] getEmbedding(String text) {
//         try {
//             return restTemplate.postForObject(bgeServerUrl, new EmbeddingRequest(text), float[].class);
//         } catch (Exception e) {
//             return null;
//         }
//     }

//     record EmbeddingRequest(String text) {}
// }