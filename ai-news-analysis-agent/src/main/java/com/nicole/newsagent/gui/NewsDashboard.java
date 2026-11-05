package com.nicole.newsagent.gui;

import com.nicole.newsagent.agent.AgentController;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.models.Timeline;
import com.nicole.newsagent.models.TimelineEvent;

// Java Swing GUI toolkit
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NewsDashboard extends JFrame {
    private final AgentController controller;
    private final DefaultListModel<Article> listModel;
    private final JList<Article> articleList;

    public NewsDashboard(AgentController controller) {
        this.controller = controller;
        setTitle("AI News Analysis Agent");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Search
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField();
        searchField.setToolTipText("Enter a topic to search (e.g., AI, Tesla, Elections)");
        //JButton fetchBtn = new JButton("Fetch News");
        JButton fetchBtn = new JButton("Fetch News (NewsAPI)");
        JButton rssBtn = new JButton("Fetch Tech News (BBC RSS)");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(fetchBtn);
        btnPanel.add(rssBtn);

        topPanel.add(new JLabel("Search Topic: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        //topPanel.add(fetchBtn, BorderLayout.EAST);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Article List 
        listModel = new DefaultListModel<>();
        articleList = new JList<>(listModel);
        articleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        articleList.setCellRenderer(new ArticleCellRenderer());
        add(new JScrollPane(articleList), BorderLayout.CENTER);

        // Bottom Panel: Buttons 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewArticleBtn = new JButton("View & Analyze Article");
        JButton clustersBtn = new JButton("Detect Duplicate Stories / Clusters");
        JButton savedBtn = new JButton("View Saved Articles");
        JButton timelineBtn = new JButton("View Timeline");
        JButton historyBtn = new JButton("Search History");

        bottomPanel.add(viewArticleBtn);
        bottomPanel.add(clustersBtn);
        bottomPanel.add(savedBtn);
        bottomPanel.add(timelineBtn);
        bottomPanel.add(historyBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Agent Chat Panel (F15) ---
        JPanel agentPanel = new JPanel(new BorderLayout(5, 5));
        agentPanel.setBorder(BorderFactory.createTitledBorder("Ask the AI Agent (Natural-Language Queries)"));

        JPanel inputRow = new JPanel(new BorderLayout(5, 5));
        JTextField queryField = new JTextField();
        queryField.setToolTipText("e.g., 'What's the latest news about technology?'");
        JButton askBtn = new JButton("Ask Agent");
        inputRow.add(queryField, BorderLayout.CENTER);
        inputRow.add(askBtn, BorderLayout.EAST);

        JTextArea answerArea = new JTextArea(6, 60);
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerArea.setEditable(false);
        answerArea.setText("Ask a question like: 'What's the latest news about AI?'\nThe agent will decide whether to search, then synthesize an answer.");
        JScrollPane answerScroll = new JScrollPane(answerArea);

        agentPanel.add(inputRow, BorderLayout.NORTH);
        agentPanel.add(answerScroll, BorderLayout.CENTER);
        agentPanel.setPreferredSize(new Dimension(0, 200));

        // Wrap center + agent panel in a vertical split so the user can resize
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(new JScrollPane(articleList), BorderLayout.CENTER);
        centerContainer.add(agentPanel, BorderLayout.SOUTH);

        // Remove previous BorderLayout.CENTER add and re-add
        //removeAll();
        add(topPanel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners --- 
        // Fetch from NewsAPI
        fetchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            fetchArticles(fetchBtn, "Fetch News (NewsAPI)", "NEWS_API", query);
        });

        // Fetch from BBC RSS
        rssBtn.addActionListener(e -> {
            fetchArticles(rssBtn, "Fetch Tech News (BBC RSS)", "RSS", "");
        });
        /* 
        fetchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            fetchBtn.setEnabled(false);
            fetchBtn.setText("Fetching...");
            listModel.clear();

            SwingWorker<List<Article>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Article> doInBackground() {
                    // Fetch using NewsAPI (you can change to "RSS" if you prefer)
                    return controller.fetchNews("NEWS_API", query);
                }

                @Override
                protected void done() {
                    try {
                        List<Article> articles = get();
                        for (Article a : articles) {
                            listModel.addElement(a);
                        }
                        if (articles.isEmpty()) {
                            JOptionPane.showMessageDialog(NewsDashboard.this, "No articles found for that topic.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(NewsDashboard.this, "Error fetching news: " + ex.getMessage());
                    }
                    fetchBtn.setEnabled(true);
                    fetchBtn.setText("Fetch News");
                }
            };
            worker.execute();
        });
        */

        // View & Analyze Article
        viewArticleBtn.addActionListener(e -> {
            Article selected = articleList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select an article from the list first.");
                return;
            }
            ArticleViewer viewer = new ArticleViewer(this, selected, controller);
            viewer.setVisible(true);
        });

        // Detect Clusters
        clustersBtn.addActionListener(e -> {
            List<List<Article>> clusters = controller.clusterStories();
            if (clusters.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No articles loaded yet. Fetch some news first.");
                return;
            }
            StringBuilder sb = new StringBuilder("Found " + clusters.size() + " story clusters:\n\n");
            int clusterNum = 1;
            boolean foundDuplicates = false;
            for (List<Article> cluster : clusters) {
                if (cluster.size() > 1) {
                    foundDuplicates = true;
                    sb.append("CLUSTER ").append(clusterNum++).append(" (").append(cluster.size()).append(" articles):\n");
                    for (Article a : cluster) {
                        sb.append("  - ").append(a.getTitle()).append("\n");
                    }
                    sb.append("\n");
                }
            }
            if (!foundDuplicates) {
                sb.append("No duplicate stories detected among the current articles.");
            }
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setPreferredSize(new Dimension(700, 400));
            JOptionPane.showMessageDialog(this, scroll, "Story Clusters", JOptionPane.INFORMATION_MESSAGE);
        });

        // View Saved Articles
        savedBtn.addActionListener(e -> {
            List<Article> saved = controller.getSavedArticles();
            if (saved.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No saved articles yet.\nOpen an article and click 'Save Article'.");
                return;
            }
            StringBuilder sb = new StringBuilder("Saved Articles:\n\n");
            for (Article a : saved) {
                sb.append("- ").append(a.getTitle()).append("\n  ").append(a.getSourceUrl()).append("\n\n");
            }
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setPreferredSize(new Dimension(700, 400));
            JOptionPane.showMessageDialog(this, scroll, "Saved Articles", JOptionPane.INFORMATION_MESSAGE);
        });

        // Ask Agent (F15)
        askBtn.addActionListener(e -> {
            String query = queryField.getText().trim();
            if (query.isEmpty()) return;
            askBtn.setEnabled(false);
            askBtn.setText("Thinking...");
            answerArea.setText("Agent is analyzing your question and deciding which tools to call.\n" +
                    "This may take 1-3 minutes on a local LLM...");

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    return controller.answerQuery(query);
                }

                @Override
                protected void done() {
                    try {
                        answerArea.setText(get());
                    } catch (Exception ex) {
                        answerArea.setText("Error: " + ex.getMessage());
                    }
                    askBtn.setEnabled(true);
                    askBtn.setText("Ask Agent");
                }
            };
            worker.execute();
        });

        // View Timeline
        timelineBtn.addActionListener(e -> {
            Timeline timeline = controller.generateTimeline();
            List<TimelineEvent> events = timeline.getEvents();
            if (events.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No articles loaded yet. Fetch some news first.");
                return;
            }
            StringBuilder sb = new StringBuilder("Chronological Timeline of Loaded Articles:\n\n");
            for (TimelineEvent ev : events) {
                sb.append(ev.getDate().toLocalDate()).append("  ").append(ev.getDate().toLocalTime().withNano(0)).append("  —  ").append(ev.getDescription()).append("\n");
            }
            showScrollableDialog("Timeline", sb.toString());
        });

        // View Search History
        historyBtn.addActionListener(e -> {
            List<String> history = controller.getSearchHistory();
            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No search history yet.");
                return;
            }
            StringBuilder sb = new StringBuilder("Search History:\n\n");
            for (int i = 0; i < history.size(); i++) {
                sb.append(i + 1).append(". ").append(history.get(i)).append("\n");
            }
            int confirm = JOptionPane.showOptionDialog(this, sb.toString(), "Search History", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Close", "Clear History"}, "Close");
            if (confirm == 1) {
                controller.clearSearchHistory();
                JOptionPane.showMessageDialog(this, "Search history cleared.");
            }
        });
    }

    private void showScrollableDialog(String title, String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(800, 450));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void fetchArticles(JButton sourceBtn, String originalText, String sourceType, String query) {
        // Record this search
        if (!query.isEmpty()) {
            controller.recordSearch(query);
        }

        sourceBtn.setEnabled(false);
        sourceBtn.setText("Fetching...");
        listModel.clear();

        SwingWorker<List<Article>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Article> doInBackground() {
                return controller.fetchNews(sourceType, query);
            }

            @Override
            protected void done() {
                try {
                    List<Article> articles = get();
                    for (Article a : articles) {
                        listModel.addElement(a);
                    }
                    if (articles.isEmpty()) {
                        JOptionPane.showMessageDialog(NewsDashboard.this, "No articles found.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NewsDashboard.this, "Error fetching news: " + ex.getMessage());
                }
                sourceBtn.setEnabled(true);
                sourceBtn.setText(originalText);
            }
        };
        worker.execute();
    }
    
    // Custom Cell Renderer to make the list look nicer
    private static class ArticleCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Article) {
                Article article = (Article) value;
                setText("<html><b>" + article.getTitle() + "</b><br/><i>" + article.getSourceUrl() + "</i></html>");
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }
            return this;
        }
    }
}
