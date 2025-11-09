package com.example.service;

import com.example.model.NewsArticle;
import com.example.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class NewsService {

    private final NewsRepository repository;
    private final GNewsClient gNewsClient;
    private final int pageSize;

    public NewsService(NewsRepository repository,
                       GNewsClient gNewsClient,
                       @Value("${news.fetch.page-size:100}") int pageSize) {
        this.repository = repository;
        this.gNewsClient = gNewsClient;
        this.pageSize = pageSize;
    }

    /**
     * Initialize DB with at least minCount articles (default 1000).
     * Fixed: continues fetching pages until minCount is reached.
     */
    public Map<String, Object> initializeBulkEnglishNews(Integer minCount, String lang) {
        if (minCount == null || minCount <= 0) minCount = 1000;
        if (lang == null || lang.isBlank()) lang = "en";

        long totalBefore = repository.count();
        int savedThisRun = 0;
        int page = 1;
        int maxPages = 1000;

        while (totalBefore + savedThisRun < minCount && page <= maxPages) {
            Map<String, Object> resp;
            try {
                resp = gNewsClient.fetchTopHeadlines(lang, page, pageSize);
            } catch (Exception e) {
                Map<String, Object> err = new HashMap<>();
                err.put("error", "GNews request failed: " + e.getMessage());
                err.put("savedThisRun", savedThisRun);
                err.put("totalInDb", repository.count());
                return err;
            }

            if (resp == null || !resp.containsKey("articles")) break;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> articles = (List<Map<String, Object>>) resp.get("articles");
            if (articles == null || articles.isEmpty()) break;

            for (Map<String, Object> m : articles) {
                String url = safeGetString(m, "url");
                if (url == null || url.isBlank() || repository.existsByUrl(url)) continue;

                NewsArticle article = mapToArticle(m, lang);
                try {
                    repository.save(article);
                    savedThisRun++;
                } catch (Exception ex) {
                    // skip duplicates or insertion errors
                }

                if (totalBefore + savedThisRun >= minCount) break;
            }

            System.out.println("Page " + page + " fetched " + articles.size() + " articles, total saved this run: " + savedThisRun);

            page++;
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requestedMin", minCount);
        result.put("savedThisRun", savedThisRun);
        result.put("totalInDb", repository.count());
        return result;
    }

    public List<NewsArticle> findAllFromDb(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        return repository.findAll(PageRequest.of(page, size)).getContent();
    }

    public Optional<NewsArticle> findById(String id) {
        return repository.findById(id);
    }

    public List<NewsArticle> findByLanguage(String lang) {
        if (lang == null || lang.isBlank()) lang = "en";
        List<NewsArticle> all = repository.findAll();
        List<NewsArticle> filtered = new ArrayList<>();
        for (NewsArticle a : all) {
            if (lang.equalsIgnoreCase(a.getLang())) filtered.add(a);
        }
        return filtered;
    }

    private NewsArticle mapToArticle(Map<String, Object> m, String defaultLang) {
        NewsArticle n = new NewsArticle();
        n.setId(safeGetString(m, "id"));
        n.setTitle(safeGetString(m, "title"));
        n.setDescription(safeGetString(m, "description"));
        n.setContent(safeGetString(m, "content"));
        n.setUrl(safeGetString(m, "url"));
        n.setImage(safeGetString(m, "image"));

        String published = safeGetString(m, "publishedAt");
        if (published != null) {
            try { n.setPublishedAt(Instant.parse(published)); } catch (Exception e) { n.setPublishedAt(Instant.now()); }
        } else n.setPublishedAt(Instant.now());

        n.setLang(safeGetString(m, "lang") != null ? safeGetString(m, "lang") : defaultLang);

        @SuppressWarnings("unchecked")
        Map<String, Object> s = (Map<String, Object>) m.get("source");
        if (s != null) {
            n.setSourceId(safeGetString(s, "id"));
            n.setSourceName(safeGetString(s, "name"));
            n.setSourceUrl(safeGetString(s, "url"));
            n.setSourceCountry(safeGetString(s, "country"));
        }
        return n;
    }

    private String safeGetString(Map<String, Object> m, String key) {
        if (m == null) return null;
        Object o = m.get(key);
        return o == null ? null : o.toString();
    }
}
