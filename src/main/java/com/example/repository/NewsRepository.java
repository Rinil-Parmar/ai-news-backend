package com.example.repository;

import com.example.model.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsRepository extends MongoRepository<NewsArticle, String> {
    boolean existsByUrl(String url);
    Optional<NewsArticle> findByUrl(String url);
    Page<NewsArticle> findAll(Pageable pageable);
}
