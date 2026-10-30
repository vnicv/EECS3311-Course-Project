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
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Search
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField();
        searchField.setToolTipText("Enter a topic to search (e.g., AI, Tesla, Elections)");
        JButton fetchBtn = new JButton("Fetch News");

        topPanel.add(new JLabel("Search Topic: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(fetchBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Article List 
        listModel = new DefaultListModel<>();
        articleList = new JList<>(listModel);
        articleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        articleList.setCellRenderer(new ArticleCellRenderer());
        add(new JScrollPane(articleList), BorderLayout.CENTER);

        // Bottom Panel: Buttons 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewArticleBtn = new JButton("View Article & Summarize");
        bottomPanel.add(viewArticleBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners 
        
        // Fetch News Action
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

        // View Article Action
        viewArticleBtn.addActionListener(e -> {
            Article selected = articleList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select an article from the list first.");
                return;
            }
            ArticleViewer viewer = new ArticleViewer(this, selected, controller);
            viewer.setVisible(true);
        });
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
