package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class GNewsClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public GNewsClient(RestTemplate restTemplate,
                       @Value("${gnews.api.key}") String apiKey,
                       @Value("${gnews.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchTopHeadlines(String lang, int page, int max) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/top-headlines")
                .queryParam("token", apiKey)
                .queryParam("lang", lang)
                .queryParam("page", page)
                .queryParam("max", max)
                .toUriString();
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.getForEntity(url, (Class<Map<String, Object>>) (Class<?>) Map.class);
            return resp.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("GNews fetchTopHeadlines failed: " + e.getMessage(), e);
        }
    }
}
