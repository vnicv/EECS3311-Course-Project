package com.nicole.newsagent.ai.strategies;

import com.nicole.newsagent.ai.LLMClient;

// F07 -- Entity Extraction
public class EntityExtraction implements AnalysisStrategy<String, String> {
    private final LLMClient llmClient;

    public EntityExtraction(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String analyze(String articleContent) {
        String prompt = "Extract all named entities (people, organizations, locations) from the following article. Return them as a comma-separated list. Example: Elon Musk, Tesla, California\n\nArticle:\n" + articleContent;
        return llmClient.generate(prompt).trim();
    }
}
