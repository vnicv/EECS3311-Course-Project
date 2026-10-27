package com.nicole.newsagent;

import com.nicole.newsagent.ai.LLMClient;
import com.nicole.newsagent.ai.strategies.AnalysisStrategy;
import com.nicole.newsagent.ai.strategies.Summarization;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.repositories.ArticleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main( String[] args ){
        // Test outputs
        System.out.println( "AI News Analysis Agent Loading..." ); 

        // Test LLMClient class
        System.out.println("Connecting to Ollama...");
        LLMClient llmClient = new LLMClient();

        // Test the models and the repositories
        System.out.println("Creating mock article and saving to ArticleRepository...\n");
        ArticleRepository articleRepo = new ArticleRepository();

        // Create fake article: fill with random generated id, fake article info, and current time
        Article mockArticle = new Article(UUID.randomUUID().toString(), "Tech Stocks Rally", "The stock market saw a massive rally today as major tech companies reported earnings that exceeded analyst expectations. Investors are optimistic about the future of AI integration in consumer products. However, some analysts warn of a potential bubble and advise caution for retail investors moving forward.", "https://example.com/news/tech-rally", LocalDateTime.now());

        // Test the ArticleRepository saving and retrieval functions
        articleRepo.save(mockArticle); // save the article in the ArticleRepository
        List<Article> allArticles = articleRepo.findAll();
        Article retrievedArticle = allArticles.get(0);
        System.out.println("Retrieved article from repository: " + retrievedArticle.getTitle() + "\n");

        // Test the strategy pattern (i.e., Summarization)
        System.out.println("Initializing Summarization Strategy...");
        AnalysisStrategy<String, String> summarizationStrategy = new Summarization(llmClient);
        System.out.println("Sending article to the LLM for summarization... (this may take a while, please wait)\n");
        String summaryResult = summarizationStrategy.analyze(retrievedArticle.getContent());

        // Print the result
        System.out.println("ARTICLE SUMMARY");
        System.out.println(summaryResult);

        /* 
        System.out.println("Sending prompt to the LLM...");
        String response = llmClient.generate("Summarize this in one sentence: Students who work need to balance their schedules. They need time for work, class, and homework. Not planning can cause problems. They won't have time for other activities if they don't plan carefully.");
        System.out.println("LLM Response:");
        System.out.println(response);
        */
    }
}
