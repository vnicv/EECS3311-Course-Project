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
        //String prompt = "Compare the following news articles covering the same event. Identify similarities, differences, and any conflicting claims:\n\n" + combinedArticles;
        
        // Explicit output cap — forces a fixed-size answer regardless of input count ()
        String prompt = "You are comparing news articles covering the same event.\n" +
                "Reply in EXACTLY this format, with NO extra text:\n\n" +
                "SIMILARITIES:\n- (one line)\n- (one line)\n\n" +
                "DIFFERENCES:\n- (one line)\n- (one line)\n\n" +
                "CONFLICTS:\n- (one line, or 'none')\n\n" +
                "Do not write introductions, conclusions, or repeat article titles.\n" +
                "Articles:\n" + combinedArticles;
        return llmClient.generate(prompt).trim();
    }
}
