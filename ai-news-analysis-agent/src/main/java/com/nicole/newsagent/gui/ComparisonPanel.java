package com.nicole.newsagent.gui;

import com.nicole.newsagent.agent.AgentController;
import com.nicole.newsagent.models.Article;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ComparisonPanel extends JDialog {
    private final AgentController controller;
    
    public ComparisonPanel(JFrame parent, AgentController controller) {
        super(parent, "Compare Sources (Cluster Comparison)", true);
        this.controller = controller;
        setSize(900, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        List<List<Article>> clusters = controller.clusterStories();

        // --- Left: cluster list ---
        DefaultListModel<String> clusterModel = new DefaultListModel<>();
        int idx = 1;
        int usableClusters = 0;
        for (List<Article> cluster : clusters) {
            if (cluster.size() >= 2) {
                clusterModel.addElement("Cluster " + idx + " (" + cluster.size() + " articles)");
                idx++;
                usableClusters++;
            }
        }
        if (clusterModel.isEmpty()) {
            clusterModel.addElement("(No clusters with 2+ articles yet)");
        }
        JList<String> clusterList = new JList<>(clusterModel);
        JScrollPane clusterScroll = new JScrollPane(clusterList);
        clusterScroll.setPreferredSize(new Dimension(250, 0));
        clusterScroll.setBorder(BorderFactory.createTitledBorder("Select a Cluster"));
        add(clusterScroll, BorderLayout.WEST);

        // --- Center: comparison report ---
        JTextArea reportArea = new JTextArea();
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setEditable(false);
        reportArea.setText("Select a cluster on the left, then click 'Compare'.");
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(BorderFactory.createTitledBorder("AI Comparison Report"));
        add(reportScroll, BorderLayout.CENTER);

        // --- Bottom: buttons + source links ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton compareBtn = new JButton("Compare Sources");
        JButton closeBtn = new JButton("Close");
        buttonRow.add(compareBtn);
        buttonRow.add(closeBtn);

        JPanel sourceRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceRow.setBorder(BorderFactory.createTitledBorder("Sources in selected cluster (F11)"));
        bottomPanel.add(buttonRow, BorderLayout.EAST);
        bottomPanel.add(sourceRow, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        final List<List<Article>> clustersRef = clusters;

        // When the user selects a cluster, show its source links as clickable buttons
        clusterList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            sourceRow.removeAll();
            int selectedIdx = clusterList.getSelectedIndex();
            if (selectedIdx < 0) return;

            // Find the corresponding cluster (skip singletons)
            int counter = 0;
            List<Article> target = null;
            for (List<Article> c : clustersRef) {
                if (c.size() >= 2) {
                    if (counter == selectedIdx) { target = c; break; }
                    counter++;
                }
            }
            if (target == null) return;

            for (Article a : target) {
                JButton srcBtn = new JButton(shortTitle(a.getTitle()));
                srcBtn.setToolTipText(a.getSourceUrl());
                srcBtn.addActionListener(ev -> controller.openSourceLink(a));
                sourceRow.add(srcBtn);
            }
            sourceRow.revalidate();
            sourceRow.repaint();
        });

        compareBtn.addActionListener(e -> {
            int selectedIdx = clusterList.getSelectedIndex();
            if (selectedIdx < 0) {
                JOptionPane.showMessageDialog(this, "Select a cluster first.");
                return;
            }

            int counter = 0;
            List<Article> target = null;
            for (List<Article> c : clustersRef) {
                if (c.size() >= 2) {
                    if (counter == selectedIdx) { target = c; break; }
                    counter++;
                }
            }
            if (target == null || target.size() < 2) {
                JOptionPane.showMessageDialog(this, "That cluster doesn't have enough articles.");
                return;
            }

            compareBtn.setEnabled(false);
            compareBtn.setText("Comparing...");
            reportArea.setText("Agent is analyzing coverage across sources (may take 1-3 minutes)...");

            final List<Article> cluster = target;
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    return controller.compareArticles(cluster);
                }

                @Override
                protected void done() {
                    try {
                        String comparison = get();
                        StringBuilder sb = new StringBuilder();
                        sb.append("=== COMPARISON OF ").append(cluster.size()).append(" SOURCES ===\n\n");
                        for (Article a : cluster) {
                            sb.append("• ").append(a.getTitle()).append("\n");
                            sb.append("  ").append(a.getSourceUrl()).append("\n\n");
                        }
                        sb.append("=== AI ANALYSIS ===\n\n");
                        sb.append(comparison);
                        reportArea.setText(sb.toString());
                        reportArea.setCaretPosition(0);
                    } catch (Exception ex) {
                        reportArea.setText("Error: " + ex.getMessage());
                    }
                    compareBtn.setEnabled(true);
                    compareBtn.setText("Compare Sources");
                }
            };
            worker.execute();
        });
        closeBtn.addActionListener(e -> dispose());
    }

    private String shortTitle(String title) {
        if (title == null) return "source";
        return title.length() > 40 ? title.substring(0, 40) + "..." : title;
    }
}
