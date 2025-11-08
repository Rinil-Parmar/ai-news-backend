package com.example.controller;

import com.example.model.ApiResponse;
import com.example.model.NewsArticle;
import com.example.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/news")
public class NewsController {

    private final NewsService service;

    public NewsController(NewsService service) {
        this.service = service;
    }

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initializePost(
            @RequestParam(defaultValue = "1000") Integer min,
            @RequestParam(defaultValue = "en") String lang) {
        Map<String, Object> result = service.initializeBulkEnglishNews(min, lang);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", (String) result.get("error"), result));
        }
        return ResponseEntity.ok(new ApiResponse<>("success", "Initialization completed", result));
    }

    @GetMapping("/init")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initializeGet(
            @RequestParam(defaultValue = "1000") Integer min,
            @RequestParam(defaultValue = "en") String lang) {
        return initializePost(min, lang);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsArticle>>> listFromDb(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(new ApiResponse<>("success", "Articles from DB", service.findAllFromDb(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsArticle>> getById(@PathVariable String id) {
        Optional<NewsArticle> maybe = service.findById(id);
        return maybe
                .map(a -> ResponseEntity.ok(new ApiResponse<>("success", "Article found", a)))
                .orElseGet(() -> ResponseEntity.ok(new ApiResponse<>("error", "Article not found", null)));
    }

    @GetMapping("/english")
    public ResponseEntity<ApiResponse<List<NewsArticle>>> getEnglishArticles() {
        return ResponseEntity.ok(new ApiResponse<>("success", "English articles", service.findByLanguage("en")));
    }
}
