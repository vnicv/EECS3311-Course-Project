package com.nicole.newsagent.sources;

import com.nicole.newsagent.models.Article;
import java.util.List;

public interface NewsSourceAdapter {
    List<Article> fetchArticles(String query);    
}
