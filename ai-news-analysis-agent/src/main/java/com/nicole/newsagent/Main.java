package com.nicole.newsagent;

/* 
import com.nicole.newsagent.ai.LLMClient;
import com.nicole.newsagent.ai.strategies.AnalysisStrategy;
import com.nicole.newsagent.ai.strategies.Summarization;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.repositories.ArticleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
*/

import com.nicole.newsagent.agent.AgentController;
import com.nicole.newsagent.models.Article;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== AI News Analysis Agent ===");
        AgentController controller = new AgentController();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Fetch Tech News");
            System.out.println("2. Summarize an Article");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<Article> fetched = controller.fetchNews("NEWS_API", "technology"); // Uses live API
                    // Or use "RSS" for the free BBC feed
                    System.out.println("Fetched " + fetched.size() + " articles.");
                    for (int i = 0; i < fetched.size() && i < 5; i++) { // Show top 5
                        System.out.println("[" + i + "] " + fetched.get(i).getTitle());
                    }
                    break;
                case "2":
                    List<Article> allArticles = controller.getAllStoredArticles();
                    if (allArticles.isEmpty()) {
                        System.out.println("No articles in memory. Fetch some first!");
                        break;
                    }
                    System.out.print("Enter article index to summarize (0-" + (allArticles.size()-1) + "): ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine());
                        Article selected = allArticles.get(index);
                        System.out.println("Summarizing: " + selected.getTitle());
                        String summary = controller.summarize(selected);
                        System.out.println("\n--- SUMMARY ---\n" + summary + "\n---------------");
                    } catch (Exception e) {
                        System.out.println("Invalid index.");
                    }
                    break;
                case "3":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    /*public static void main( String[] args ){
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
    }
    */
}
