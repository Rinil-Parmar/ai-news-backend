package com.example.controller;

import com.example.model.ApiResponse;
import com.example.newsapi.model.NewsArticle;
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

    // Primary: use POST to trigger heavy API calls
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

    // Convenience GET (in case you want to call from browser) — still triggers initialization
    @GetMapping("/init")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initializeGet(
            @RequestParam(defaultValue = "1000") Integer min,
            @RequestParam(defaultValue = "en") String lang) {
        return initializePost(min, lang);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsArticle>>> listFromDb(
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<NewsArticle> data = service.findAllFromDb(page, size);
        return ResponseEntity.ok(new ApiResponse<>("success", "Articles from DB", data));
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
        List<NewsArticle> data = service.findByLanguage("en");
        return ResponseEntity.ok(new ApiResponse<>("success", "English articles", data));
    }
}
