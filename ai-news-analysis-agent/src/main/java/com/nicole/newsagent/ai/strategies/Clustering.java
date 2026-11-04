package com.nicole.newsagent.ai.strategies;

import com.nicole.newsagent.models.Article;
import java.util.*;

// F05 -- Duplicate-Story Detection
// F06 -- Event Clustering
public class Clustering implements AnalysisStrategy<List<Article>, List<List<Article>>> {
    
    @Override
    public List<List<Article>> analyze(List<Article> articles) {
        List<List<Article>> clusters = new ArrayList<>();
        
        for (Article article : articles) {
            boolean added = false;
            for (List<Article> cluster : clusters) {
                // Simple similarity check: if titles share 50%+ of words, same cluster
                if (calculateSimilarity(article.getTitle(), cluster.get(0).getTitle()) > 0.5) {
                    cluster.add(article);
                    added = true;
                    break;
                }
            }

            if (!added) {
                List<Article> newCluster = new ArrayList<>();
                newCluster.add(article);
                clusters.add(newCluster);
            }
        }
        return clusters;
    }

    // Computes Jaccard Similarity score between two strings based on their distinct word sets (case sensitive)
    private double calculateSimilarity(String s1, String s2) {
        // Tokenize each string into a set of unique words
        Set<String> w1 = new HashSet<>(Arrays.asList(s1.toLowerCase().split("\\W+"))); // example: "Hello, World!" => ["hello", "world"]
        Set<String> w2 = new HashSet<>(Arrays.asList(s2.toLowerCase().split("\\W+")));

        Set<String> intersection = new HashSet<>(w1); // intersection: words that appear in both strings
        intersection.retainAll(w2);
        Set<String> union = new HashSet<>(w1); // union: every distinct word across both strings
        union.addAll(w2);

        if (union.isEmpty()) {
            return 0;
        }
        // Jaccard index = |intersection| / |union|
        return (double) intersection.size() / union.size();
    }
}
