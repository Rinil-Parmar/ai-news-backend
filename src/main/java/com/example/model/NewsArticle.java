package com.example.newsapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "news")
public class NewsArticle {
    @Id
    private String id;            // GNews article id (may be null)
    private String title;
    private String description;
    private String content;
    @Indexed(unique = true, sparse = true)
    private String url;           // unique index to avoid duplicates
    private String image;
    private Instant publishedAt;
    private String lang;

    // Flattened source fields
    private String sourceId;
    private String sourceName;
    private String sourceUrl;
    private String sourceCountry;
}
