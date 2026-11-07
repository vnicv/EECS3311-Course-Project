package com.nicole.newsagent.ai.strategies;

import com.nicole.newsagent.models.Article;
import java.util.*;

// F05 -- Duplicate-Story Detection
// F06 -- Event Clustering
public class Clustering implements AnalysisStrategy<List<Article>, List<List<Article>>> {
    // Threshold for "same event" 
    private static final double SIMILARITY_THRESHOLD = 0.20;

    // Common English stopwords to ignore during similarity
    private static final Set<String> STOPWORDS = new HashSet<>(Set.of(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "be",
            "been", "being", "have", "has", "had", "do", "does", "did",
            "would", "could", "should", "may", "might", "must", "can", "this",
            "that", "these", "those", "it", "its", "they", "them", "their",
            "he", "she", "his", "her", "we", "our", "you", "your", "i", "me",
            "my", "not", "no", "so", "if", "than", "then", "there", "here",
            "what", "when", "where", "who", "how", "why", "which", "about",
            "up", "out", "into", "over", "after", "before", "more", "most",
            "some", "any", "all", "each", "every", "other", "such", "only",
            "own", "same", "just", "also", "very", "too", "s", "t", "will",
            "says", "said", "say", "new", "one", "two", "three", "first", "last"
    ));

    @Override
    public List<List<Article>> analyze(List<Article> articles) {
        List<List<Article>> clusters = new ArrayList<>();
        
        for (Article article : articles) {
            boolean added = false;
            for (List<Article> cluster : clusters) {
                // Compare against the cluster representative (first article)
                double similarity = combinedSimilarity(article, cluster.get(0));
                if (similarity >= SIMILARITY_THRESHOLD) {
                    cluster.add(article);
                    added = true;
                    break;
                }
            }
            /* 
            for (List<Article> cluster : clusters) {
                // Simple similarity check: if titles share 50%+ of words, same cluster
                if (calculateSimilarity(article.getTitle(), cluster.get(0).getTitle()) > 0.5) {
                    cluster.add(article);
                    added = true;
                    break;
                }
            }
            */

            if (!added) {
                List<Article> newCluster = new ArrayList<>();
                newCluster.add(article);
                clusters.add(newCluster);
            }
        }
        return clusters;
    }

    // Combined similarity: 60% title, 40% content preview.
    private double combinedSimilarity(Article a, Article b) {
        double titleSim = jaccard(wordSet(a.getTitle()), wordSet(b.getTitle()));
        double contentSim = jaccard(wordSet(preview(a.getContent())), wordSet(preview(b.getContent())));

        // Returns a score between 0.0 and 1.0.
        return (titleSim * 0.6) + (contentSim * 0.4); // title is weighted more heavily because it's the strongest "topic" signal
    }

    // Take the first N characters of the content, so long scraped articles don't dominate 
    private String preview(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 500 ? content.substring(0, 500) : content;
    }

    // Turn text into a set of lowercase words (Drop stopwords and short tokens)
    private Set<String> wordSet(String text) {
        if (text == null) return Collections.emptySet();
        Set<String> words = new HashSet<>();
        for (String w : text.toLowerCase().split("\\W+")) {
            if (w.length() >= 3 && !STOPWORDS.contains(w)) {
                words.add(w);
            }
        }
        return words;
    }

    // Jaccard similarity: |A ∩ B| / |A ∪ B| 
    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }

    /* 
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
    */
}
