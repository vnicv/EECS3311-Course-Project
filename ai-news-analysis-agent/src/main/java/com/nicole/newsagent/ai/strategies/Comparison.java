package com.nicole.newsagent.ai.strategies;

import com.nicole.newsagent.ai.LLMClient;

// F09 -- Multiple Source Comparison 
public class Comparison implements AnalysisStrategy<String, String> {
    private final LLMClient llmClient;

    public Comparison(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String analyze(String combinedArticles) {
        String prompt = "Compare the following news articles covering the same event. Identify similarities, differences, and any conflicting claims:\n\n" + combinedArticles;
        return llmClient.generate(prompt).trim();
    }
}
