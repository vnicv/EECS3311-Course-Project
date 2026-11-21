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
//import com.nicole.newsagent.models.Article;

import javax.swing.*;
//import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CliOptions opts = CliOptions.parse(args);

        // Help / parse errors → print usage and exit
        if (opts.getMode() == CliOptions.Mode.HELP) {
            for (String e : opts.getErrors()) System.err.println("[ERROR] " + e);
            System.out.println(CliOptions.usage());
            System.exit(opts.getErrors().isEmpty() ? 0 : 2);
            return;
        }

        System.out.println("AI News Analysis Agent Loading...");
        AgentController controller = new AgentController();

        switch (opts.getMode()) {
            case GUI -> launchGui(controller);

            case CLI -> {
                NewsCLI cli = new NewsCLI(controller, System.in, System.out, System.err);
                int code = cli.runInteractive(true);
                System.exit(code);
            }

            case SCRIPT -> {
                NewsCLI cli = new NewsCLI(controller, System.in, System.out, System.err);
                int code = cli.runScript(Path.of(opts.getScriptFile()));
                System.exit(code);
            }

            default -> runChooser(controller);
        }
    }

    private static void runChooser(AgentController controller) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nSelect Mode:");
        System.out.println("1. Command Line Interface (CLI)");
        System.out.println("2. Graphical User Interface (GUI)");
        System.out.print("Choice: ");

        String choice = scanner.nextLine();

        if (choice.trim().equals("2")) {
            launchGui(controller);
        } else {
            NewsCLI cli = new NewsCLI(controller, scanner, System.out, System.err);
            System.exit(cli.runInteractive(false));
        }
    }

    private static void launchGui(AgentController controller) {
        SwingUtilities.invokeLater(() -> {
            NewsDashboard dashboard = new NewsDashboard(controller);
            dashboard.setVisible(true);
        });
    }
}
/* 
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
}
*/
