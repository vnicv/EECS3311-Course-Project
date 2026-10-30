package com.nicole.newsagent.gui;

import com.nicole.newsagent.agent.AgentController;
import com.nicole.newsagent.models.Article;

// Java Swing GUI toolkit
import javax.swing.*;
import java.awt.*;

public class ArticleViewer extends JDialog {
    private final Article article;
    private final AgentController controller;

    public ArticleViewer(JFrame parent, Article article, AgentController controller) {
        super(parent, "Article Viewer - " + article.getTitle(), true);
        this.article = article;
        this.controller = controller;

        setSize(700, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Title & URL 
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("<html><h2>" + article.getTitle() + "</h2></html>");
        topPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel urlLabel = new JLabel("<html><a href='" + article.getSourceUrl() + "'>" + article.getSourceUrl() + "</a></html>");
        urlLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        urlLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(article.getSourceUrl()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Could not open link.");
                }
            }
        });
        topPanel.add(urlLabel, BorderLayout.SOUTH); 
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Article Content 
        JTextArea contentArea = new JTextArea(article.getContent());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // Bottom Panel: AI Summary 
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("AI Summary"));
        
        JTextArea summaryArea = new JTextArea(6, 50);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setEditable(false);
        summaryArea.setText("Click 'Generate Summary' to analyze this article...");
        bottomPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        JButton summarizeBtn = new JButton("Generate Summary");
        summarizeBtn.addActionListener(e -> {
            summarizeBtn.setEnabled(false);
            summarizeBtn.setText("Analyzing (Please wait)...");
            summaryArea.setText("The local LLM is reading the article and generating a summary. This may take a few seconds...");
            
            // Run LLM call in a background thread to prevent the GUI from freezing
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    return controller.summarize(article);
                }

                @Override
                protected void done() {
                    try {
                        summaryArea.setText(get());
                    } catch (Exception ex) {
                        summaryArea.setText("Error generating summary: " + ex.getMessage());
                    }
                    summarizeBtn.setEnabled(true);
                    summarizeBtn.setText("Generate Summary");
                }
            };
            worker.execute();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(summarizeBtn);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
