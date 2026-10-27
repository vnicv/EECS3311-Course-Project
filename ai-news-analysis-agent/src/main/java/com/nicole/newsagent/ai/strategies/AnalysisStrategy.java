package com.nicole.newsagent.ai.strategies;

/*public class AnalysisStrategy {
    
}*/

public interface AnalysisStrategy<T, R> {
    R analyze(T input);
}
