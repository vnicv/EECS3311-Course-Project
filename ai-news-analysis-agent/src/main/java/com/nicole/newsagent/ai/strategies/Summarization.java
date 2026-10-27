package com.nicole.newsagent.ai.strategies;

import com.nicole.newsagent.ai.LLMClient;

public class Summarization implements AnalysisStrategy<String, String> {
    private final LLMClient llmClient;

    public Summarization(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override 
    public String analyze(String articleContent) {
        String prompt = "Summarize the following news article in a maximum of 3 short and concise bullet points. Do not add introductory or closing remarks. Focus on the main takeaway:\n\n" + articleContent;
        return llmClient.generate(prompt);
    }
}
