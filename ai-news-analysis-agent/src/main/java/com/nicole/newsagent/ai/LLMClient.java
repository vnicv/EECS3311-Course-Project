package com.nicole.newsagent.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class LLMClient {
    private final ChatLanguageModel chatModel;

    // Connect to Ollama running locally on port 11434
    public LLMClient() {
        this.chatModel = OllamaChatModel.builder().baseUrl("http://localhost:11434").modelName("llama3").temperature(0.7).build();
    }

    public String generate(String prompt) {
        return chatModel.generate(prompt);
    }
}
