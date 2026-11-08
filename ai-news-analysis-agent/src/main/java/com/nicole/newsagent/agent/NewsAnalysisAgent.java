package com.nicole.newsagent.agent;

import com.nicole.newsagent.ai.LLMClient;
//import com.nicole.newsagent.ai.strategies.AnalysisStrategy;
//import com.nicole.newsagent.ai.strategies.Summarization;
import com.nicole.newsagent.ai.strategies.*;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.models.Timeline;
import com.nicole.newsagent.models.TimelineEvent;
import com.nicole.newsagent.models.Topic;
import com.nicole.newsagent.repositories.ArticleRepository;
import com.nicole.newsagent.repositories.SearchHistoryRepository;
import com.nicole.newsagent.repositories.TopicRepository;
import com.nicole.newsagent.sources.NewsSourceAdapter;
import com.nicole.newsagent.sources.NewsSourceFactory;
import com.google.gson.annotations.Expose;
import com.nicole.newsagent.agent.NewsAgentTools;
import com.nicole.newsagent.agent.PlanningEngine;
import com.nicole.newsagent.agent.TopicTracker;
import java.util.List;
import java.util.UUID;

// Construct the agent, and wire each strategy to the shared LLM client
public class NewsAnalysisAgent {
    private final LLMClient llmClient;
    private final ArticleRepository articleRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final TopicRepository topicRepository;
    private final PlanningEngine planningEngine;
    private final TopicTracker topicTracker;
    
    private final Summarization summarization;
    private final Categorization categorization;
    private final EntityExtraction entityExtraction;
    private final ClaimExtraction claimExtraction;
    private final Comparison comparison;
    private final Clustering clustering;

    public NewsAnalysisAgent(LLMClient llmClient, ArticleRepository articleRepository) {
        this.llmClient = llmClient;
        this.articleRepository = articleRepository;
        this.searchHistoryRepository = new SearchHistoryRepository();
        this.topicRepository = new TopicRepository();
        this.summarization = new Summarization(llmClient);
        this.categorization = new Categorization(llmClient);
        this.entityExtraction = new EntityExtraction(llmClient);
        this.claimExtraction = new ClaimExtraction(llmClient);
        this.comparison = new Comparison(llmClient);
        this.clustering = new Clustering();
        this.topicTracker = new TopicTracker();

        NewsAgentTools tools = new NewsAgentTools(articleRepository);
        this.planningEngine = new PlanningEngine(llmClient.getChatModel(), tools);
    }

    // --- Topic Management (F01) ---
    public Topic createTopic(String name) {
        // Avoid duplicates
        Topic existing = topicRepository.findByName(name);
        if (existing != null) {
            return existing;
        }
        Topic topic = new Topic(UUID.randomUUID().toString(), name);
        topicRepository.save(topic);
        return topic;
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    public List<Article> getAllStoredArticles() {
        return articleRepository.findAll();
    }

    public boolean deleteTopic(String id) {
        return topicRepository.delete(id);
    }

    public List<Article> retrieveArticles(String sourceType, String query) {
        // Give the correct adapter for the given source type
        //NewsSourceAdapter adapter = NewsSourceFactory.createAdapter(sourceType);
        NewsSourceAdapter adapter = NewsSourceFactory.createAdapter(sourceType, query);
        List<Article> articles = adapter.fetchArticles(query);
        // Save article to repository
        for (Article article : articles) {
            articleRepository.save(article);
        }
        return articles;
    }

    // --- News Analysis Strategies ---
    public String summarize(Article article) { 
        return summarization.analyze(article.getContent()); 
    }

    public String categorize(Article article) { 
        return categorization.analyze(article.getContent()); 
    }

    public String extractEntities(Article article) { 
        return entityExtraction.analyze(article.getContent()); 
    }

    public String extractClaims(Article article) { 
        return claimExtraction.analyze(article.getContent()); 
    }

    // Compare a set of articles by concatenating each URL and content into one prompt
    // Then ask LLM to analyze them together
    public String compare(List<Article> articles) {
        // Build a single combined prompt with each article's source and content
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (Article a : articles) {
            // Truncate each article to 500 chars so the combined prompt stays manageable
            String content = a.getContent() == null ? "" : a.getContent();
            if (content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            sb.append("--- Article ").append(idx++).append(" ---\n");
            sb.append("Source: ").append(a.getSourceUrl()).append("\n");
            sb.append("Title: ").append(a.getTitle()).append("\n");
            sb.append("Preview: ").append(content).append("\n\n");
        }
        // Cap the total prompt length in case there are 10+ articles
        String combined = sb.toString();
        if (combined.length() > 3000) {
            combined = combined.substring(0, 3000) + "\n...[truncated for length]";
        }
        return comparison.analyze(combined);
    }

    // Group related articles into clusters based on content similarity
    public List<List<Article>> clusterArticles(List<Article> articles) { 
        return clustering.analyze(articles); 
    }

    // Timeline generation
    public Timeline generateTimeline(List<Article> articles) {
        // Sort by publish date - real timeline generation
        Timeline timeline = new Timeline();
        for (Article a : articles) {
            timeline.addEvent(new TimelineEvent(a.getId(), a.getTitle(), a.getPublishedDate()));
        }
        return timeline;
    }

    // Daily Briefing Generation
    public String generateDailyBriefing() {
        List<Article> all = articleRepository.findAll();
        if (all.isEmpty()) return "No articles available. Fetch some news first.";

        List<List<Article>> clusters = clusterArticles(all);
        clusters.sort((a, b) -> Integer.compare(b.size(), a.size()));

        StringBuilder briefing = new StringBuilder();
        briefing.append("=== DAILY BRIEFING ===\n");
        briefing.append("Generated from ").append(all.size()).append(" articles across ")
                .append(clusters.size()).append(" story clusters.\n\n");

        int clusterNum = 1;
        for (List<Article> cluster : clusters) {
            if (cluster.isEmpty()) continue;
            Article lead = cluster.get(0);

            briefing.append("--- Story ").append(clusterNum++).append(" (").append(cluster.size()).append(" article(s)) ---\n");
            briefing.append("Headline: ").append(lead.getTitle()).append("\n");
            briefing.append("Source: ").append(lead.getSourceUrl()).append("\n");

            // Fast extractive summary — no LLM call
            briefing.append(extractiveSummary(lead.getContent(), 2)).append("\n\n");
        }
        return briefing.toString();
    }

    // Extract the first N meaningful sentences from the text
    private String extractiveSummary(String text, int sentenceCount) {
        if (text == null || text.isEmpty()) return "";
        String clean = text.replaceAll("\\s+", " ").trim();
        String[] sentences = clean.split("(?<=[.!?])\\s+");
        StringBuilder sb = new StringBuilder();
        int taken = 0;
        for (String s : sentences) {
            if (s.length() < 30) continue; // skip fragments
            sb.append(s.trim()).append(" ");
            if (++taken >= sentenceCount) break;
        }
        return sb.toString().trim();
    }

    // Personalized Feed 
    public List<Article> generatePersonalizedFeed() {
        List<Article> all = articleRepository.findAll();
        if (all.isEmpty()) return all;

        List<Topic> topics = topicRepository.findAll();
        List<String> tracked = topicTracker.getTrackedTopics();
        List<String> history = searchHistoryRepository.findAll();

        return all.stream().sorted((a, b) -> Double.compare(scoreArticle(b, topics, tracked, history), scoreArticle(a, topics, tracked, history))).toList();
    }

    private double scoreArticle(Article a, List<Topic> topics, List<String> tracked, List<String> history) {
        String text = (a.getTitle() + " " + a.getContent()).toLowerCase();
        double score = 0;

        // Saved articles: strong boost
        if (a.isSaved()) score += 5.0;

        // Match against user's saved Topic objects (F01) — strongest signal
        for (Topic t : topics) {
            if (text.contains(t.getName().toLowerCase())) score += 4.0;
        }

        // Match against tracked topic names (F12)
        for (String topic : tracked) {
            if (text.contains(topic.toLowerCase())) score += 3.0;
        }

        // Match against search history keywords (F17)
        for (String query : history) {
            for (String word : query.toLowerCase().split("\\s+")) {
                if (word.length() > 3 && text.contains(word)) score += 1.0;
            }
        }
        return score;
    }

    // --- Search History ---
    public void recordSearch(String query) {
        searchHistoryRepository.save(query);
    }

    public List<String> getSearchHistory() {
        return searchHistoryRepository.findAll();
    }

    public void clearSearchHistory() {
        searchHistoryRepository.clear();
    }

    // --- Topic Tracking ---
    public TopicTracker getTopicTracker() { 
        return topicTracker; 
    }

    public void trackTopic(String topic) { 
        topicTracker.trackTopic(topic); 
        createTopic(topic); 
    }

    public void untrackTopic(String topic) { 
        topicTracker.untrackTopic(topic); 
    }

    public List<String> getTrackedTopics() { 
        return topicTracker.getTrackedTopics(); 
    }

    public void simulateTopicRefresh() {
        List<String> tracked = topicTracker.getTrackedTopics();
        for (String topic : tracked) {
            // Fetch new articles for this tracked topic
            List<Article> newArticles = retrieveArticles("NEWS_API", topic);
            if (!newArticles.isEmpty()) {
                topicTracker.notifyObservers(topic, newArticles.size());
            }
        }
    }

    // Natural Language Query
    public String answerQuery(String query) {
        recordSearch(query);  // Save to search history
        return planningEngine.answerQuery(query);
    }
}

/*
public class NewsAnalysisAgent {
    private final LLMClient llmClient;
    private final ArticleRepository articleRepository;
    private final AnalysisStrategy<String, String> summarizationStrategy;

    public NewsAnalysisAgent(LLMClient llmClient, ArticleRepository articleRepository) {
        this.llmClient = llmClient;
        this.articleRepository = articleRepository;
        this.summarizationStrategy = new Summarization(llmClient);
    }

    public List<Article> retrieveArticles(String sourceType, String query) {
        NewsSourceAdapter adapter = NewsSourceFactory.createAdapter(sourceType);
        List<Article> articles = adapter.fetchArticles(query);
        
        // Save to repository
        for (Article article : articles) {
            articleRepository.save(article);
        }
        return articles;
    }

    public String summarizeArticle(Article article) {
        return summarizationStrategy.analyze(article.getContent());
    }
}
*/
