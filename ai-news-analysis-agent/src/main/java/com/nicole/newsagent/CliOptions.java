package com.nicole.newsagent;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses command-line arguments for the AI News Analysis Agent.
 *
 * Supported options:
 *   -cli                  Force interactive CLI mode (skip GUI/CLI chooser)
 *   -gui                  Launch the Swing GUI directly
 *   -input <file>         Run commands from <file> non-interactively, then exit
 *   -script <file>        Alias for -input
 *   -help                 Print CLI usage and exit
 *   -h                    Alias for -help
 */
public class CliOptions {
    public enum Mode { CHOOSER, CLI, GUI, SCRIPT, HELP }

    private Mode mode = Mode.CHOOSER;
    private String scriptFile = null;
    private final List<String> errors = new ArrayList<>();

    public static CliOptions parse(String[] args) {
        CliOptions opts = new CliOptions();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-cli":
                    opts.mode = Mode.CLI;
                    break;
                case "-gui":
                    opts.mode = Mode.GUI;
                    break;
                case "-help":
                case "-h":
                    opts.mode = Mode.HELP;
                    break;
                case "-input":
                case "-script":
                    if (i + 1 >= args.length) {
                        opts.errors.add("Option '" + a + "' requires a file path.");
                    } else {
                        opts.scriptFile = args[++i];
                        opts.mode = Mode.SCRIPT;
                    }
                    break;
                default:
                    opts.errors.add("Unknown option: " + a);
            }
        }
        // -help / errors take precedence over other modes
        if (opts.mode == Mode.HELP) return opts;
        if (!opts.errors.isEmpty()) opts.mode = Mode.HELP;
        return opts;
    }

    public Mode getMode() { return mode; }
    public String getScriptFile() { return scriptFile; }
    public List<String> getErrors() { return errors; }

    public static String usage() {
        return String.join("\n",
            "AI News Analysis Agent — Command-Line Interface",
            "",
            "USAGE:",
            "  java -jar ai-news-analysis-agent.jar [OPTIONS]",
            "",
            "OPTIONS:",
            "  -cli                 Start the interactive CLI (skip GUI/CLI chooser).",
            "  -gui                 Launch the Swing GUI directly.",
            "  -input <file>        Run commands from <file> non-interactively, then exit.",
            "  -script <file>       Alias for -input.",
            "  -help, -h            Show this help message.",
            "",
            "  (no options)         Show the '1=CLI, 2=GUI' chooser.",
            "",
            "STDIN:",
            "  You can also pipe commands into -cli mode, e.g.:",
            "    cat tasks.txt | java -jar ai-news-analysis-agent.jar -cli",
            "",
            "SCRIPT FILE FORMAT:",
            "  One command per line. Blank lines and lines beginning with '#' are ignored.",
            "  Lines that fail print an [ERROR] message and execution continues.",
            "",
            "COMMANDS:",
            "  help                             Show in-shell help",
            "  fetch <newsapi|rss> <query>      Fetch articles (query may contain spaces)",
            "  list [all|saved]                 List stored articles (default: all)",
            "  summarize <index>                AI summary of article at <index>",
            "  categorize <index>               AI category of article at <index>",
            "  entities <index>                 AI named-entity extraction",
            "  claims <index>                   AI factual-claim extraction",
            "  save <index>                     Mark article as saved",
            "  source <index>                   Print the source URL of article <index>",
            "  clusters                         Detect duplicate/clustered stories",
            "  compare <index>                  Compare the cluster containing article <index>",
            "  compare-cluster <n>              Compare cluster <n> (1-based, from 'clusters')",
            "  timeline                         Chronological timeline of loaded articles",
            "  briefing                         Generate the daily briefing",
            "  feed                             Personalized feed (ranked)",
            "  history                          Show search history",
            "  history clear                    Clear search history",
            "  topic create <name>              Create a topic",
            "  topic list                       List topics",
            "  topic delete <id|name>           Delete a topic by id or name",
            "  track <name>                     Begin tracking a topic",
            "  untrack <name>                   Stop tracking a topic",
            "  tracked                          List tracked topics",
            "  refresh-tracked                  Refresh articles for tracked topics",
            "  ask <question>                   Natural-language query to the AI agent",
            "  exit                             Exit the CLI",
            "",
            "EXAMPLES:",
            "  java -jar ai-news-analysis-agent.jar -cli",
            "  java -jar ai-news-analysis-agent.jar -input example-tasks.txt",
            "  echo \"fetch rss technology\" | java -jar ai-news-analysis-agent.jar -cli",
            ""
        );
    }
}
