package com.nicole.newsagent.ai.strategies;

import com.nicole.newsagent.ai.LLMClient;

// F10 -- Claim Extraction
public class ClaimExtraction implements AnalysisStrategy<String, String> {
    private final LLMClient llmClient;

    public ClaimExtraction(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String analyze(String articleContent) {
        String prompt = "Extract the 3 most important factual claims made in this article. Return them as a numbered list.\n\nArticle:\n" + articleContent;
        return llmClient.generate(prompt).trim();
    }
}
