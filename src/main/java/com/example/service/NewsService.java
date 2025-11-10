package com.example.service;

import com.example.model.NewsArticle;
import com.example.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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
                       @Value("${news.fetch.page-size:200}") int pageSize) {
        this.repository = repository;
        this.gNewsClient = gNewsClient;
        this.pageSize = pageSize;
    }

    /**
     * Initialize DB with at least minCount English articles
     */
    public Map<String, Object> initializeBulkEnglishNews(Integer minCount, String lang) {
        if (minCount == null || minCount <= 0) minCount = 500;
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

            List<NewsArticle> batchToSave = new ArrayList<>();
            for (Map<String, Object> m : articles) {
                // only English articles
                String detectedLang = safeGetString(m, "lang");
                if (detectedLang != null && !detectedLang.isBlank() && !"en".equalsIgnoreCase(detectedLang)) {
                    continue;
                }

                String url = safeGetString(m, "url");
                if (url == null || url.isBlank()) continue;

                // skip duplicates
                if (repository.existsByUrl(url)) continue;

                NewsArticle article = mapToArticle(m, "en");
                article.setLang("en");
                batchToSave.add(article);
            }

            if (!batchToSave.isEmpty()) {
                try {
                    repository.saveAll(batchToSave);
                    savedThisRun += batchToSave.size();
                } catch (DataIntegrityViolationException dive) {
                    // skip duplicates
                } catch (Exception ex) {
                    System.out.println("Batch save error: " + ex.getMessage());
                }
            }

            System.out.println("Page " + page + " fetched " + articles.size()
                    + " articles, saved this run: " + savedThisRun
                    + ", total in db: " + repository.count());

            page++;
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
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

    public List<NewsArticle> findByLanguage(String lang, int page, int size) {
        if (lang == null || lang.isBlank()) lang = "en";
        return repository.findByLang(lang.toLowerCase(), PageRequest.of(page, size)).getContent();
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

        String payloadLang = safeGetString(m, "lang");
        n.setLang(payloadLang != null ? payloadLang.toLowerCase() : defaultLang.toLowerCase());

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
