package com.nicole.newsagent.gui;

import com.nicole.newsagent.agent.AgentController;
import com.nicole.newsagent.models.Article;

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
        setSize(1000, 700);
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

        bottomPanel.add(viewArticleBtn);
        bottomPanel.add(clustersBtn);
        bottomPanel.add(savedBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners 
        
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
    }

    private void fetchArticles(JButton sourceBtn, String originalText, String sourceType, String query) {
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
