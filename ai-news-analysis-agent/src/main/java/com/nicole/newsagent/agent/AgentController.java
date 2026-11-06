package com.nicole.newsagent.agent;

import com.nicole.newsagent.ai.LLMClient;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.models.Timeline;
import com.nicole.newsagent.models.Topic;
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
        //System.out.println("Agent: Fetching articles from " + sourceType + " for '" + query + "'...");
        return agent.retrieveArticles(sourceType, query);
    }

    // --- Topic Creation ---
    public Topic createTopic(String name) {
        return agent.createTopic(name);
    }

    public List<Topic> getAllTopics() {
        return agent.getAllTopics();
    }

    public boolean deleteTopic(String id) {
        return agent.deleteTopic(id);
    }

    // --- Single Article Analysis
    public String summarize(Article article) { 
        return agent.summarize(article); 
    }

    public String categorize(Article article) { 
        return agent.categorize(article); 
    }

    public String extractEntities(Article article) { 
        return agent.extractEntities(article); 
    }

    public String extractClaims(Article article) { 
        return agent.extractClaims(article); 
    }

    // --- Multi-Article Analysis
    public String compareArticles(List<Article> articles) { 
        return agent.compare(articles); 
    }

    public List<List<Article>> clusterStories() { 
        return agent.clusterArticles(articleRepository.findAll()); 
    }

    // --- Timeline ---
    public Timeline generateTimeline() {
        return agent.generateTimeline(articleRepository.findAll());
    }

    // --- Daily Briefing ---
    public String generateDailyBriefing() {
        return agent.generateDailyBriefing();
    }  

    // --- Personalized Feed ---
    public List<Article> generatePersonalizedFeed() {
        return agent.generatePersonalizedFeed();
    }

    // --- Article Storage ---
    public List<Article> getAllStoredArticles() {
        return articleRepository.findAll();
    }

    public void saveArticle(Article article) { 
        article.setSaved(true); 
    }

    public List<Article> getSavedArticles() {
        return articleRepository.findAll().stream().filter(Article::isSaved).toList();
    }

    // --- Search History ---
    public void recordSearch(String query) { 
        agent.recordSearch(query); 
    }

    public List<String> getSearchHistory() { 
        return agent.getSearchHistory(); 
    }

    public void clearSearchHistory() { 
        agent.clearSearchHistory(); 
    }

    // --- Topic Tracking ---
    public void trackTopic(String topic) { 
        agent.trackTopic(topic); 
    }

    public void untrackTopic(String topic) { 
        agent.untrackTopic(topic); 
    }

    public List<String> getTrackedTopics() { 
        return agent.getTrackedTopics(); 
    }

    public com.nicole.newsagent.agent.TopicTracker getTopicTracker() { 
        return agent.getTopicTracker(); 
    }

    public void simulateTopicRefresh() { 
        agent.simulateTopicRefresh(); 
    }

    // Natural Language Query
    public String answerQuery(String query) {
        return agent.answerQuery(query);
    }
}
