package com.nicole.newsagent;

import com.nicole.newsagent.ai.LLMClient;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main( String[] args ){
        // Test outputs
        System.out.println( "AI News Analysis Agent Loading..." ); 

        // Test LLMClient class
        System.out.println("Connecting to Ollama...");
        LLMClient llmClient = new LLMClient();

        System.out.println("Sending prompt to the LLM...");
        String response = llmClient.generate("Summarize this in one sentence: Students who work need to balance their schedules. They need time for work, class, and homework. Not planning can cause problems. They won't have time for other activities if they don't plan carefully.");
        System.out.println("LLM Response:");
        System.out.println(response);
    }
}
