package com.nicole.newsagent.repositories;

import com.nicole.newsagent.models.Article;
import java.util.ArrayList;
import java.util.List;

public class ArticleRepository {
    private final List<Article> articles = new ArrayList<>();

    public void save(Article article) {
        articles.add(article);
    }

    public Article findById(String id) {
        // return the first article with the given id, return null if it doesn't exist
        return articles.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Article> findAll() {
        return new ArrayList<>(articles);
    }
}
