package com.nicole.newsagent.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class LLMClient {
    private final ChatLanguageModel chatModel;

    // Connect to Ollama running locally on port 11434
    public LLMClient() {
        // Note: temperature value ==> higher is more creative (1.0), lower is more coherant/factually accurate (0.1) 
        this.chatModel = OllamaChatModel.builder().baseUrl("http://localhost:11434").modelName("llama3").temperature(0.5).build();
    }

    public String generate(String prompt) {
        return chatModel.generate(prompt);
    }
}
