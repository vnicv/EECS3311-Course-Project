# AI News Analysis Agent

A Java-based AI news analysis system that uses a **locally hosted large language model** (Ollama + Llama 3) to retrieve, analyze, cluster, and summarize news from multiple sources (BBC RSS and NewsAPI). The system provides both a **command-line interface (CLI)** and a **graphical user interface (GUI)** built with Java Swing.

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Dependency Installation](#2-dependency-installation)
3. [Local Configuration (API Key Setup)](#3-local-configuration-api-key-setup)
4. [Building the Project](#4-building-the-project)
5. [Launching the Application](#5-launching-the-application)
6. [Example Usage](#6-example-usage)
7. [Features](#7-features)
8. [Design Patterns](#8-design-patterns)
9. [Troubleshooting](#9-troubleshooting)
10. [Project Structure](#10-project-structure)

---

## 1. Prerequisites

Before running the application, install the following:

| Tool | Minimum Version | Notes |
|------|-----------------|-------|
| **Java Development Kit (JDK)** | 17 | `java -version` should report 17 or higher |
| **Apache Maven** | 3.8+ | `mvn -version` |
| **Ollama** | Latest | [Download from ollama.com](https://ollama.com) |
| **Llama 3 model** | — | Downloaded via Ollama (`ollama pull llama3`) |
| **NewsAPI key** (free) | — | [Sign up at newsapi.org](https://newsapi.org) |

**Optional but recommended:**
- 8 GB+ RAM (16 GB for smoother LLM inference)
- 5 GB+ free disk space (for the LLM model)

---

## 2. Dependency Installation

### 2.1 Install Ollama and Llama 3

1. Download and install Ollama from [https://ollama.com/download](https://ollama.com/download).
2. Open a terminal and verify the installation:
   ```bash
   ollama --version
   ```
3. Pull the model used by the app:
   ```bash
   ollama pull llama3
   ```
4. Start the Ollama service (if not already running in the background):
   ```bash
   ollama serve
   ```
   Leave this terminal open while using the app.

### 2.2 Install Maven dependencies

All Java dependencies (LangChain4j, Jackson, Rome, Jsoup, dotenv-java, JUnit 5) are managed by Maven. Nothing needs to be installed manually — see [Section 4: Building](#4-building-the-project).

---

## 3. Local Configuration (API Key Setup)

This project uses a NewsAPI.org API key. **Never commit real keys to GitHub** — they are ignored by `.gitignore`.

### 3.1 Create your local `.env` file

In the **root** of the project (the folder containing `pom.xml`), create a file named `.env`:

```
NEWS_API_KEY=your_actual_newsapi_key_here
```

Get a free key at [https://newsapi.org/register](https://newsapi.org/register).

### 3.2 The example file

A safe example file `.env.example` is committed to the repository:

```
NEWS_API_KEY=YOUR_NEWS_API_KEY_HERE
```

To set up your local configuration:

```bash
# Copy the example file
cp .env.example .env

# Edit .env and paste your real key
```

**On Windows PowerShell:**
```powershell
Copy-Item .env.example .env
notepad .env
```

> ⚠️ The `.env` file is listed in `.gitignore` and will **never** be committed. Only `.env.example` is tracked in Git.

---

## 4. Building the Project

From the project root (`ai-news-analysis-agent/`):

```bash
# Clean and build (downloads dependencies + compiles + runs tests)
mvn clean package
```

The first build may take 1–2 minutes while Maven downloads dependencies. On subsequent builds, it's much faster.

To skip tests during a quick build:
```bash
mvn clean package -DskipTests
```

A shaded, runnable JAR is created at:
```
target/ai-news-analysis-agent-1.0-SNAPSHOT.jar
```

---

## 5. Launching the Application

The app supports both a **CLI** and a **GUI**. You'll be asked to choose on startup.

### 5.1 Launch via Maven (recommended for development)

```bash
mvn exec:java -Dexec.mainClass="com.nicole.newsagent.Main"
```

### 5.2 Launch via the shaded JAR

```bash
java -jar target/ai-news-analysis-agent-1.0-SNAPSHOT.jar
```

### 5.3 Launch from an IDE (VS Code, IntelliJ, Eclipse)

Open `src/main/java/com/nicole/newsagent/Main.java` and click **Run**.

### 5.4 Choose interface

On startup you'll see:

```
AI News Analysis Agent Loading...

Select Mode:
1. Command Line Interface (CLI)
2. Graphical User Interface (GUI)
Choice:
```

Enter `1` for CLI, `2` for GUI.

> **Make sure Ollama is running** before you try to use any AI feature. Without it, the app will start but LLM analysis will fail with a connection error.

---

## 6. Example Usage

### 6.1 GUI walkthrough (recommended for demos)

1. **Launch the GUI** (`Main` → choose `2`).
2. **Create a topic** — a welcome dialog appears asking for your first topic (e.g., `Technology`).
3. **Fetch news** — type a query in the search bar (e.g., `Tesla`) and click:
   - **Fetch News (NewsAPI)** — 10 articles from newsapi.org
   - **Fetch News (BBC RSS)** — up to 5 articles from BBC (category feeds) or Google News RSS (free-text)
4. **Analyze an article** — select any article from the list and click **View & Analyze Article**. Inside the viewer:
   - **Summarize** — 3-bullet AI summary
   - **Categorize** — one of 8 predefined categories
   - **Extract Entities** — people, organizations, locations
   - **Extract Claims** — factual claims for fact-checking
   - **Save Article** — mark for later
   - **View Source** — open the original URL in your browser
5. **Detect duplicate stories** — click **Detect Duplicate Stories / Clusters** to group related articles by similarity.
6. **Compare sources** — click **Compare Sources**, select a cluster, and click **Compare Sources** to get an AI-generated comparison of how each outlet covered the event.
7. **Timeline** — click **View Timeline** to see all loaded articles sorted chronologically.
8. **Daily Briefing** — click **Daily Briefing** for a synthesized summary of the top stories.
9. **Personalized Feed** — click **My Feed** for a relevance-ranked feed based on your topics, tracked topics, saved articles, and search history.
10. **Ask the AI Agent** — type a natural-language question in the chat panel at the bottom (e.g., `"What's the latest news about AI?"`) and click **Ask Agent**. The LLM autonomously decides whether to call the news-search tools.

### 6.2 CLI walkthrough

1. **Launch the CLI** (`Main` → choose `1`).
2. Use the numbered menu:
   ```
   Menu:
   1. Fetch News (NewsAPI)
   2. Fetch Tech News (BBC RSS)
   3. Summarize an Article
   4. Exit
   ```
3. Type `1`, then a query such as `Tesla`. The CLI fetches articles and shows the first 5 titles.
4. Type `3`, then an index to summarize an article.

### 6.3 Example search queries

| Query | Source | Result |
|-------|--------|--------|
| `Tesla` | NewsAPI | Latest Tesla news (10 articles) |
| `technology` | RSS | BBC Technology feed |
| `sports` | RSS | BBC Sports feed |
| `OpenAI` | RSS | Google News RSS for OpenAI |
| `election` | NewsAPI | Election coverage across outlets |

---

## 7. Features

All 17 features from the Stage 1 design are implemented:

| ID | Feature | AI Involvement |
|----|---------|----------------|
| F01 | Topic Creation | Deterministic |
| F02 | Article Retrieval | Deterministic |
| F03 | Article Categorization | AI-based |
| F04 | Article Summarization | AI-based |
| F05 | Duplicate-Story Detection | AI-based |
| F06 | Event Clustering | Hybrid |
| F07 | Entity Extraction | AI-based |
| F08 | Timeline Generation | Hybrid |
| F09 | Multiple Source Comparison | AI-based |
| F10 | Claim Extraction | AI-based |
| F11 | Source Linking | Deterministic |
| F12 | Topic Tracking | Hybrid |
| F13 | Personalized Feed | Hybrid |
| F14 | Saved Articles | Deterministic |
| F15 | Natural-Language News Queries | AI-based (agent w/ tool-calling) |
| F16 | Daily Briefing | Hybrid |
| F17 | Search History | Deterministic |

---

## 8. Design Patterns

| Pattern | Where it's used |
|---------|-----------------|
| **Strategy** | `AnalysisStrategy<T,R>` interface + `Summarization`, `Categorization`, `EntityExtraction`, `ClaimExtraction`, `Comparison`, `Clustering` |
| **Factory Method** | `NewsSourceFactory.createAdapter()` selects `RSSAdapter` or `NewsAPIAdapter` |
| **Adapter** | `RSSAdapter` (Rome + Jsoup), `NewsAPIAdapter` (Jackson + Jsoup) |
| **Facade** | `AgentController` wraps `NewsAnalysisAgent`, repositories, `PlanningEngine`, and `SourceLinkManager` |
| **MVC** | Models (`Article`, `Topic`, `EventCluster`, `Timeline`, ...), Views (`NewsDashboard`, `ArticleViewer`, `ComparisonPanel`, `NewsCLI`), Controllers (`AgentController`, `NewsController`) |
| **Observer** | `TopicTracker` notifies registered `Observer`s (`NewsDashboard`) when new articles arrive for a tracked topic |

---

## 9. Troubleshooting

### "Connection refused" or "timeout" errors when running analysis

**Cause:** Ollama is not running, or the model isn't loaded.

**Fix:**
```bash
# Check Ollama is running
curl http://localhost:11434/api/tags

# Restart if needed
ollama serve
```

### "NEWS_API_KEY not found" error

**Cause:** The `.env` file is missing or in the wrong location.

**Fix:** Ensure `.env` is in the project root, not inside `src/`.

### NewsAPI returns 0 articles

**Cause:** Free-tier quota exhausted (100 requests/day) or rate-limited.

**Fix:** Wait until the next day, or switch to BBC RSS (no key required).

### LLM analysis takes 2+ minutes

**Cause:** Local LLMs are slower on CPU-only machines.

**Fix:**
- Use a smaller model: `ollama pull qwen2.5:0.5b`
- Change `modelName("llama3")` → `modelName("qwen2.5:0.5b")` in `LLMClient.java`
- Reduce the number of articles fetched per query

### Timeout during comparison or briefing

**Cause:** Long prompts / many articles on slow CPU.

**Fix:** Lower the per-article character cap in `NewsAnalysisAgent.compare()` (currently 100 chars), or switch to a smaller model.

### RSS scraping fails

**Cause:** The target site blocked Jsoup or requires a JavaScript-enabled browser.

**Fix:** The app automatically falls back to the RSS description field. Full article scraping is best-effort.

---

## 10. Project Structure

```
ai-news-analysis-agent/
├── .env.example              # Safe template for API key config
├── .gitignore                # Ignores .env, target/, etc.
├── pom.xml                   # Maven dependencies and build plugins
├── README.md                 # This file
├── stage2-report.pdf         # Stage 2 Implementation Report (submission)
└── src/main/java/com/nicole/newsagent/
    ├── Main.java             # Entry point (CLI/GUI selector)
    ├── ai/
    │   ├── LLMClient.java    # LangChain4j + Ollama wrapper
    │   └── strategies/       # Strategy pattern implementations
    │       ├── AnalysisStrategy.java
    │       ├── Summarization.java
    │       ├── Categorization.java
    │       ├── EntityExtraction.java
    │       ├── ClaimExtraction.java
    │       ├── Comparison.java
    │       └── Clustering.java
    ├── agent/
    │   ├── NewsAnalysisAgent.java   # The AI agent
    │   ├── AgentController.java     # Facade for GUI/CLI
    │   ├── PlanningEngine.java      # Agent tool-calling (F15)
    │   ├── NewsAgentTools.java      # Tools the agent can call
    │   ├── SourceLinkManager.java   # F11
    │   └── TopicTracker.java        # Observer pattern (F12)
    ├── gui/
    │   ├── NewsDashboard.java       # Main window
    │   ├── ArticleViewer.java       # Article detail + analysis
    │   └── ComparisonPanel.java     # F09
    ├── models/
    │   ├── Article.java
    │   ├── Topic.java
    │   ├── Entity.java
    │   ├── Claim.java
    │   ├── Timeline.java
    │   └── TimelineEvent.java
    ├── repositories/
    │   ├── ArticleRepository.java
    │   ├── TopicRepository.java
    │   └── SearchHistoryRepository.java
    └── sources/
        ├── NewsSourceAdapter.java
        ├── RSSAdapter.java
        ├── NewsAPIAdapter.java
        └── NewsSourceFactory.java
```

---

## API Key Safety

- `.env` is **git-ignored** and never committed.
- Only `.env.example` (placeholders) is tracked in Git.
- If you accidentally commit a real key, **revoke it immediately** at [newsapi.org/account](https://newsapi.org/account) and generate a new one. Deleting the file from a later commit does not remove it from Git history.