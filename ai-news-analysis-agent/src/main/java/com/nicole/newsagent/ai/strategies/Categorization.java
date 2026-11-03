package com.nicole.newsagent.ai.strategies;

import com.nicole.newsagent.ai.LLMClient;

// F03 -- Article Categorization
public class Categorization implements AnalysisStrategy<String, String> {
    private final LLMClient llmClient;

    public Categorization(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String analyze(String articleContent) {
        String prompt = "Categorize the following news article into exactly one of these categories: Politics, Sports, Technology, Business, Health, Science, Entertainment, World. Reply with only the category name and nothing else.\n\nArticle:\n" + articleContent;
        return llmClient.generate(prompt).trim();
    }
}
