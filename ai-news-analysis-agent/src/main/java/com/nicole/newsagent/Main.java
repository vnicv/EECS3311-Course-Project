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
import com.nicole.newsagent.gui.NewsDashboard;
import com.nicole.newsagent.models.Article;

import javax.swing.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("AI News Analysis Agent Loading...");
        AgentController controller = new AgentController();

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nSelect Mode:");
        System.out.println("1. Command Line Interface (CLI)");
        System.out.println("2. Graphical User Interface (GUI)");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();

        if (choice.equals("2")) {
            // Launch Swing GUI on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                NewsDashboard dashboard = new NewsDashboard(controller);
                dashboard.setVisible(true);
            });
        } else {
            // Run CLI
            runCLI(controller, scanner);
        }
    }

    private static void runCLI(AgentController controller, Scanner scanner) {
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Fetch News (NewsAPI)");
            System.out.println("2. Fetch Tech News (BBC RSS)");
            System.out.println("3. Summarize an Article");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter search query: ");
                    String query = scanner.nextLine();
                    List<Article> fetched = controller.fetchNews("NEWS_API", query);
                    System.out.println("Fetched " + fetched.size() + " articles.");
                    for (int i = 0; i < fetched.size() && i < 5; i++) {
                        System.out.println("[" + i + "] " + fetched.get(i).getTitle());
                    }
                    break;
                case "2":
                    List<Article> rssFetched = controller.fetchNews("RSS", "");
                    System.out.println("Fetched " + rssFetched.size() + " RSS articles.");
                    break;
                case "3":
                    List<Article> allArticles = controller.getAllStoredArticles();
                    if (allArticles.isEmpty()) {
                        System.out.println("No articles in memory. Fetch some first!");
                        break;
                    }
                    System.out.print("Enter article index (0-" + (allArticles.size()-1) + "): ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine());
                        Article selected = allArticles.get(index);
                        System.out.println("Summarizing (please wait)...");
                        String summary = controller.summarize(selected);
                        System.out.println("\n--- SUMMARY ---\n" + summary + "\n---------------");
                    } catch (Exception e) {
                        System.out.println("Invalid index.");
                    }
                    break;
                case "4":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    /*
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
                    for (int i = 0; i < fetched.size() && i < 10; i++) { // Show top 5
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
    */
}
