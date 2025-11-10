package com.example.controller;

import com.example.model.ApiResponse;
import com.example.model.NewsArticle;
import com.example.model.User;
import com.example.service.NewsService;
import com.example.service.UserService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/ml")
public class MLController {

    private final NewsService newsService;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final String mlBaseUrl = "http://localhost:8000"; // FastAPI base URL

    public MLController(NewsService newsService, UserService userService, RestTemplate restTemplate) {
        this.newsService = newsService;
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    // ================= Summarize =================
    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<Map<String, String>>> summarize(@RequestBody Map<String, String> body) {
        String articleId = body.get("articleId");
        if (articleId == null || articleId.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "articleId required", null));
        }

        Optional<NewsArticle> articleOpt = newsService.findById(articleId);
        if (articleOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "Article not found", null));
        }

        Map<String, String> mlRequest = new HashMap<>();
        mlRequest.put("content", articleOpt.get().getContent());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(mlRequest, headers);

        Map<String, String> mlResponse;
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(mlBaseUrl + "/summarize", request, Map.class);
            mlResponse = response.getBody();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "ML service call failed: " + e.getMessage(), null));
        }

        return ResponseEntity.ok(new ApiResponse<>("success", "Summary fetched from ML service", mlResponse));
    }

    // ================= Recommend =================
    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<Object>> recommend(@RequestBody Map<String, Object> body) {
        Map<String, Object> mlRequest = new HashMap<>();

        String userId = (String) body.get("userId");
        if (userId != null && !userId.isBlank()) {
            Optional<User> userOpt = userService.getById(userId);
            if (userOpt.isPresent()) {
                mlRequest.put("preferences", userOpt.get().getPreferences());
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse<>("error", "User not found", null));
            }
        } else if (body.containsKey("preferences")) {
            mlRequest.put("preferences", body.get("preferences"));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "userId or preferences required", null));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(mlRequest, headers);

        Object mlResponse;
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(mlBaseUrl + "/recommend", request, Object.class);
            mlResponse = response.getBody();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "ML service call failed: " + e.getMessage(), null));
        }

        return ResponseEntity.ok(new ApiResponse<>("success", "Recommendations fetched from ML service", mlResponse));
    }
}
