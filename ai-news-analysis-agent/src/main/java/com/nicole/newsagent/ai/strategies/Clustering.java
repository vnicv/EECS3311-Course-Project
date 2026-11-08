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
        int n = articles.size();
        if (n == 0) {
            return new ArrayList<>();
        }

        // Step 1: compute pairwise similarity matrix
        double[][] sim = new double[n][n];
        for (int i = 0; i < n; i++) {
            sim[i][i] = 1.0;
            for (int j = i + 1; j < n; j++) {
                double s = combinedSimilarity(articles.get(i), articles.get(j));
                sim[i][j] = s;
                sim[j][i] = s;
            }
        }

        // Step 2: build connected components (union-find) using threshold
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (sim[i][j] >= SIMILARITY_THRESHOLD) {
                    int ri = find(parent, i);
                    int rj = find(parent, j);
                    if (ri != rj) parent[ri] = rj;
                }
            }
        }

        // Step 3: group articles by component root
        Map<Integer, List<Article>> groups = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            int root = find(parent, i);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(articles.get(i));
        }
        return new ArrayList<>(groups.values());
    }

    private int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];  // path compression
            i = parent[i];
        }
        return i;
    }
    /* 
    @Override
    public List<List<Article>> analyze(List<Article> articles) {
        List<List<Article>> clusters = new ArrayList<>();
        for (Article article : articles) {
            boolean added = false;
            for (List<Article> cluster : clusters) {
                // [FIXED] Compare against EVERY article in the cluster, take the best match
                // This removes the "first article wins" ordering bias
                double bestSim = 0.0;
                for (Article existing : cluster) {
                    double sim = combinedSimilarity(article, existing);
                    if (sim > bestSim) bestSim = sim;
                }
                if (bestSim >= SIMILARITY_THRESHOLD) {
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
 
        // for (Article article : articles) {
        //     boolean added = false;
        //     for (List<Article> cluster : clusters) {
        //         // Compare against the cluster representative (first article)
        //         double similarity = combinedSimilarity(article, cluster.get(0));
        //         if (similarity >= SIMILARITY_THRESHOLD) {
        //             cluster.add(article);
        //             added = true;
        //             break;
        //         }
        //     }

        //     if (!added) {
        //         List<Article> newCluster = new ArrayList<>();
        //         newCluster.add(article);
        //         clusters.add(newCluster);
        //     }
        // }
        // return clusters;

    }
    */

    // Combined similarity: 60% title, 40% content preview.
    private double combinedSimilarity(Article a, Article b) {
        double titleSim = jaccard(wordSet(a.getTitle()), wordSet(b.getTitle()));
        double contentSim = jaccard(wordSet(preview(a.getContent())), wordSet(preview(b.getContent())));

        // Add a 0.05 bonus if both URLs point to the same host (same outlet)
        double hostBonus = sameHost(a.getSourceUrl(), b.getSourceUrl()) ? 0.05 : 0.0;
        // Returns a score between 0.0 and 1.0.
        return (titleSim * 0.6) + (contentSim * 0.4) + hostBonus;
    }

    private boolean sameHost(String url1, String url2) {
        if (url1 == null || url2 == null) {
            return false;
        }
        try {
            String h1 = new java.net.URI(url1).getHost();
            String h2 = new java.net.URI(url2).getHost();
            return h1 != null && h1.equals(h2);
        } catch (Exception e) {
            return false;
        }
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
