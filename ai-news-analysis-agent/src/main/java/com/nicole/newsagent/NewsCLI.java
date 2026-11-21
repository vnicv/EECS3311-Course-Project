package com.nicole.newsagent;

import com.nicole.newsagent.agent.AgentController;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.models.Timeline;
import com.nicole.newsagent.models.TimelineEvent;
import com.nicole.newsagent.models.Topic;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Standalone CLI for the AI News Analysis Agent.
 *
 * Runs either interactively (from a terminal) or non-interactively (from a
 * script file or piped stdin). Every command routes through the same
 * {@link AgentController} facade that the GUI uses, so all 17 features are
 * reachable from the CLI.
 */
public class NewsCLI {
    private final AgentController controller;
    private final BufferedReader in;
    private final PrintStream out;
    private final PrintStream err;
    private boolean lastCommandFailed = false;
    private Scanner scannerIn;

    public NewsCLI(AgentController controller, InputStream in, PrintStream out, PrintStream err) {
        this(controller, new InputStreamReader(in, StandardCharsets.UTF_8), out, err);
    }

    public NewsCLI(AgentController controller, Reader in, PrintStream out, PrintStream err) {
        this.controller = controller;
        this.in = (in instanceof BufferedReader br) ? br : new BufferedReader(in);
        this.out = out;
        this.err = err;
    }

    public NewsCLI(AgentController controller, Scanner scanner, PrintStream out, PrintStream err) {
        this.controller = controller;
        this.in = null;              // not used in Scanner mode
        this.scannerIn = scanner;
        this.out = out;
        this.err = err;
    }

    /** Interactive loop: reads one command per line until 'exit' or EOF. */
    public int runInteractive(boolean showBanner) {
        if (showBanner) {
            out.println("AI News Analysis Agent — interactive CLI");
            out.println("Type 'help' for a list of commands, or 'exit' to quit.");
            out.println();
        }
        while (true) {
            if (showBanner) out.print("newsagent> ");
            String line;
            try {
                line = (scannerIn != null) ? scannerIn.nextLine() : in.readLine();
            } catch (Exception e) {
                err.println("[ERROR] Failed to read input: " + e.getMessage());
                return 1;
            }
            if (line == null) { // EOF
                if (showBanner) out.println();
                return lastCommandFailed ? 1 : 0;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            if (!executeOne(trimmed)) {
                return lastCommandFailed ? 1 : 0;
            }
        }
    }

    /** Non-interactive: run every non-comment line from the file, continue on error. */
    public int runScript(Path scriptFile) {
        if (!Files.exists(scriptFile)) {
            err.println("[ERROR] Script file not found: " + scriptFile.toAbsolutePath());
            return 1;
        }
        out.println("=== Running script: " + scriptFile + " ===");
        int lineNo = 0;
        try (BufferedReader reader = Files.newBufferedReader(scriptFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                out.println();
                out.println(">>> line " + lineNo + ": " + trimmed);
                boolean keepGoing = executeOne(trimmed);
                if (!keepGoing) {
                    out.println("=== Script finished (exit encountered) ===");
                    return lastCommandFailed ? 1 : 0;
                }
            }
        } catch (IOException e) {
            err.println("[ERROR] Failed to read script: " + e.getMessage());
            return 1;
        }
        out.println();
        out.println("=== Script finished (" + lineNo + " lines) ===");
        return lastCommandFailed ? 1 : 0;
    }

    /**
     * Executes a single command line. Returns false iff the command was 'exit'/'quit'.
     * Errors are printed to stderr and set lastCommandFailed=true; parsing is
     * deliberately lenient so scripts can continue.
     */
    private boolean executeOne(String line) {
        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String rest = parts.length > 1 ? parts[1].trim() : "";

        try {
            switch (cmd) {
                case "help":
                    out.println(CliOptions.usage());
                    return true;
                case "exit":
                case "quit":
                    out.println("Bye.");
                    return false;
                case "fetch":      doFetch(rest); break;
                case "list":       doList(rest); break;
                case "summarize":  doSummarize(rest); break;
                case "categorize": doCategorize(rest); break;
                case "entities":   doEntities(rest); break;
                case "claims":     doClaims(rest); break;
                case "save":       doSave(rest); break;
                case "source":     doSource(rest); break;
                case "clusters":   doClusters(); break;
                case "compare":    doCompareArticle(rest); break;
                case "compare-cluster": doCompareCluster(rest); break;
                case "timeline":   doTimeline(); break;
                case "briefing":   doBriefing(); break;
                case "feed":       doFeed(); break;
                case "history":    doHistory(rest); break;
                case "topic":      doTopic(rest); break;
                case "track":      doTrack(rest); break;
                case "untrack":    doUntrack(rest); break;
                case "tracked":    doTracked(); break;
                case "refresh-tracked": doRefreshTracked(); break;
                case "ask":        doAsk(rest); break;
                default:
                    err.println("[ERROR] Unknown command: " + cmd + " (type 'help')");
                    lastCommandFailed = true;
            }
        } catch (Exception e) {
            err.println("[ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            lastCommandFailed = true;
        }
        return true;
    }

    // ---------- command implementations ----------

    private void doFetch(String rest) {
        String[] parts = rest.split("\\s+", 2);
        if (parts.length < 1 || parts[0].isEmpty()) {
            fail("Usage: fetch <newsapi|rss> <query>");
            return;
        }
        String source = parts[0].toLowerCase();
        String query = parts.length > 1 ? parts[1] : "";
        String sourceType = switch (source) {
            case "newsapi", "news-api", "api" -> "NEWS_API";
            case "rss", "bbc" -> "RSS";
            default -> null;
        };
        if (sourceType == null) {
            fail("Unknown source '" + source + "'. Use 'newsapi' or 'rss'.");
            return;
        }
        if (!query.isEmpty()) controller.recordSearch(query);
        out.println("Fetching from " + sourceType + (query.isEmpty() ? "" : " for '" + query + "'") + "...");
        List<Article> fetched = controller.fetchNews(sourceType, query);
        out.println("Fetched " + fetched.size() + " articles.");
        for (int i = 0; i < fetched.size(); i++) {
            out.println("  " + i + ". " + fetched.get(i).getTitle());
        }
        if (fetched.isEmpty()) lastCommandFailed = true;
    }

    private void doList(String rest) {
        List<Article> articles;
        if (rest.equalsIgnoreCase("saved")) {
            articles = controller.getSavedArticles();
            out.println("Saved articles (" + articles.size() + "):");
        } else {
            articles = controller.getAllStoredArticles();
            out.println("Stored articles (" + articles.size() + "):");
        }
        for (int i = 0; i < articles.size(); i++) {
            Article a = articles.get(i);
            String marker = a.isSaved() ? " [SAVED]" : "";
            out.println("  " + i + ". " + a.getTitle() + marker);
            out.println("       " + a.getSourceUrl());
        }
        if (articles.isEmpty()) lastCommandFailed = true;
    }

    private void doSummarize(String rest) {
        Article a = articleAt(rest);
        if (a == null) return;
        out.println("Summarizing '" + a.getTitle() + "' (this may take 10-60s)...");
        out.println(controller.summarize(a));
    }

    private void doCategorize(String rest) {
        Article a = articleAt(rest);
        if (a == null) return;
        out.println("Categorizing...");
        out.println("Category: " + controller.categorize(a));
    }

    private void doEntities(String rest) {
        Article a = articleAt(rest);
        if (a == null) return;
        out.println("Extracting entities...");
        out.println(controller.extractEntities(a));
    }

    private void doClaims(String rest) {
        Article a = articleAt(rest);
        if (a == null) return;
        out.println("Extracting claims...");
        out.println(controller.extractClaims(a));
    }

    private void doSave(String rest) {
        Article a = articleAt(rest);
        if (a == null) return;
        controller.saveArticle(a);
        out.println("Saved: " + a.getTitle());
    }

    private void doSource(String rest) {
        Article a = articleAt(rest);
        if (a == null) return;
        out.println(controller.getSourceLink(a));
    }

    private void doClusters() {
        List<List<Article>> clusters = controller.clusterStories();
        if (clusters.isEmpty()) {
            out.println("No articles loaded yet. Fetch some news first.");
            lastCommandFailed = true;
            return;
        }
        out.println("Found " + clusters.size() + " clusters:");
        int n = 1;
        boolean anyMulti = false;
        for (List<Article> c : clusters) {
            if (c.size() > 1) {
                anyMulti = true;
                out.println("  Cluster " + n + " (" + c.size() + " articles):");
                for (Article a : c) out.println("    - " + a.getTitle());
            }
            n++;
        }
        if (!anyMulti) out.println("  (No clusters with 2+ articles)");
    }

    private void doCompareArticle(String rest) {
        Article target = articleAt(rest);
        if (target == null) return;
        List<List<Article>> clusters = controller.clusterStories();
        List<Article> cluster = null;
        for (List<Article> c : clusters) {
            if (c.contains(target) && c.size() >= 2) { cluster = c; break; }
        }
        if (cluster == null) {
            fail("Article is not part of a cluster with 2+ articles. Run 'clusters' first.");
            return;
        }
        out.println("Comparing " + cluster.size() + " articles (this may take 1-3 minutes)...");
        out.println(controller.compareArticles(cluster));
    }

    private void doCompareCluster(String rest) {
        int n;
        try {
            n = Integer.parseInt(rest.trim());
        } catch (NumberFormatException e) {
            fail("Usage: compare-cluster <n>  (n is 1-based, from 'clusters')");
            return;
        }
        List<List<Article>> clusters = controller.clusterStories();
        // Use the same numbering that 'clusters' prints (1-based, all clusters)
        if (n < 1 || n > clusters.size()) {
            fail("Cluster " + n + " does not exist (have " + clusters.size() + ").");
            return;
        }
        List<Article> cluster = clusters.get(n - 1);
        if (cluster.size() < 2) {
            fail("Cluster " + n + " has only 1 article; nothing to compare.");
            return;
        }
        out.println("Comparing cluster " + n + " (" + cluster.size() + " articles)...");
        out.println(controller.compareArticles(cluster));
    }

    private void doTimeline() {
        Timeline t = controller.generateTimeline();
        List<TimelineEvent> events = t.getEvents();
        if (events.isEmpty()) {
            out.println("No articles loaded yet.");
            lastCommandFailed = true;
            return;
        }
        out.println("Timeline (" + events.size() + " events):");
        for (TimelineEvent e : events) {
            out.println("  " + e.getDate() + "  —  " + e.getDescription());
        }
    }

    private void doBriefing() {
        out.println("Generating daily briefing...");
        out.println(controller.generateDailyBriefing());
    }

    private void doFeed() {
        List<Article> feed = controller.generatePersonalizedFeed();
        if (feed.isEmpty()) {
            out.println("No articles loaded yet.");
            lastCommandFailed = true;
            return;
        }
        out.println("Personalized feed (" + feed.size() + " items):");
        int limit = Math.min(15, feed.size());
        for (int i = 0; i < limit; i++) {
            Article a = feed.get(i);
            out.println("  " + (i + 1) + ". " + a.getTitle() + (a.isSaved() ? " [SAVED]" : ""));
            out.println("      " + a.getSourceUrl());
        }
    }

    private void doHistory(String rest) {
        if (rest.equalsIgnoreCase("clear")) {
            controller.clearSearchHistory();
            out.println("Search history cleared.");
            return;
        }
        List<String> h = controller.getSearchHistory();
        if (h.isEmpty()) {
            out.println("(No search history)");
            return;
        }
        out.println("Search history:");
        for (int i = 0; i < h.size(); i++) out.println("  " + (i + 1) + ". " + h.get(i));
    }

    private void doTopic(String rest) {
        String[] parts = rest.split("\\s+", 2);
        if (parts.length == 0 || parts[0].isEmpty()) {
            fail("Usage: topic <create|list|delete> [args]");
            return;
        }
        String sub = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1].trim() : "";
        switch (sub) {
            case "create" -> {
                if (args.isEmpty()) { fail("Usage: topic create <name>"); return; }
                Topic t = controller.createTopic(args);
                out.println("Topic created: " + t.getName() + " (" + t.getId() + ")");
            }
            case "list" -> {
                List<Topic> topics = controller.getAllTopics();
                if (topics.isEmpty()) { out.println("(No topics)"); return; }
                out.println("Topics:");
                for (Topic t : topics) out.println("  - " + t.getName() + "  id=" + t.getId());
            }
            case "delete" -> {
                if (args.isEmpty()) { fail("Usage: topic delete <id|name>"); return; }
                List<Topic> topics = controller.getAllTopics();
                String targetId = null;
                for (Topic t : topics) {
                    if (t.getId().equals(args) || t.getName().equalsIgnoreCase(args)) {
                        targetId = t.getId();
                        break;
                    }
                }
                if (targetId == null) { fail("No topic matches '" + args + "'."); return; }
                boolean ok = controller.deleteTopic(targetId);
                out.println(ok ? "Topic deleted." : "Topic not found.");
                if (!ok) lastCommandFailed = true;
            }
            default -> fail("Unknown topic subcommand: " + sub);
        }
    }

    private void doTrack(String rest) {
        if (rest.isEmpty()) { fail("Usage: track <name>"); return; }
        controller.trackTopic(rest);
        out.println("Now tracking: " + rest);
    }

    private void doUntrack(String rest) {
        if (rest.isEmpty()) { fail("Usage: untrack <name>"); return; }
        controller.untrackTopic(rest);
        out.println("Stopped tracking: " + rest);
    }

    private void doTracked() {
        List<String> tracked = controller.getTrackedTopics();
        if (tracked.isEmpty()) { out.println("(No tracked topics)"); return; }
        out.println("Tracked topics:");
        for (String t : tracked) out.println("  - " + t);
    }

    private void doRefreshTracked() {
        out.println("Refreshing tracked topics (may take a while)...");
        controller.simulateTopicRefresh();
        out.println("Refresh complete.");
    }

    private void doAsk(String rest) {
        if (rest.isEmpty()) { fail("Usage: ask <question>"); return; }
        out.println("Agent is thinking (may take 1-3 minutes)...");
        out.println(controller.answerQuery(rest));
    }

    // ---------- helpers ----------

    private Article articleAt(String rest) {
        int idx;
        try {
            idx = Integer.parseInt(rest.trim());
        } catch (NumberFormatException e) {
            fail("Expected an article index (see 'list'). Got: '" + rest + "'");
            return null;
        }
        List<Article> all = controller.getAllStoredArticles();
        if (idx < 0 || idx >= all.size()) {
            fail("Index " + idx + " out of range (have " + all.size() + " articles).");
            return null;
        }
        return all.get(idx);
    }

    private void fail(String msg) {
        err.println("[ERROR] " + msg);
        lastCommandFailed = true;
    }
}
