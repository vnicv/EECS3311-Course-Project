package com.nicole.newsagent.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

public class PlanningEngine {

    public interface NewsAssistant {
        @SystemMessage({
            "You are an AI news analysis assistant.",
            "You have tools to search for news and access stored articles.",
            "When the user asks a question:",
            "1. FIRST decide if you need to search for fresh news. If the topic is 'current' or 'recent', use searchNews.",
            "2. If a search fails, try searchTechNewsRSS as a fallback.",
            "3. Use getStoredArticles to reference articles already loaded.",
            "4. Then answer the user's question using the retrieved information.",
            "Always cite the article titles and source URLs when answering.",
            "If you don't have enough information, say so clearly."
        })
        String chat(String userMessage);
    }

    private final NewsAssistant assistant;

    public PlanningEngine(ChatLanguageModel chatModel, NewsAgentTools tools) {
        this.assistant = AiServices.builder(NewsAssistant.class).chatLanguageModel(chatModel).tools(tools).build();
    }

    public String answerQuery(String query) {
        return assistant.chat(query);
    }
}
