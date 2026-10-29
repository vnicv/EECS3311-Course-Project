package com.nicole.newsagent.agent;

import com.nicole.newsagent.ai.LLMClient;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.repositories.ArticleRepository;
import java.util.List;

public class AgentController {
    private final NewsAnalysisAgent agent;
    private final ArticleRepository articleRepository;

    public AgentController() {
        // Initialize the core system internally
        LLMClient llmClient = new LLMClient();
        this.articleRepository = new ArticleRepository();
        this.agent = new NewsAnalysisAgent(llmClient, articleRepository);
    }

    // --- Facade methods for the UI to call ---

    public List<Article> fetchNews(String sourceType, String query) {
        System.out.println("Agent: Fetching articles from " + sourceType + " for '" + query + "'...");
        return agent.retrieveArticles(sourceType, query);
    }

    public String summarize(Article article) {
        System.out.println("Agent: Analyzing and summarizing article...");
        return agent.summarizeArticle(article);
    }

    public List<Article> getAllStoredArticles() {
        return articleRepository.findAll();
    }
}
