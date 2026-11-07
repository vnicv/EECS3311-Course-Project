package com.nicole.newsagent.agent;

import com.nicole.newsagent.models.Article;

import java.awt.Desktop;
import java.net.URI;

public class SourceLinkManager {
    // Return the source URL for an article, or empty string if not available
    public String getSourceLink(Article article) {
        if (article == null || article.getSourceUrl() == null) {
            return "";
        }
        return article.getSourceUrl();
    }

    // Open the article's source URL in the system's default browser
    public boolean openSourceLink(Article article) {
        String url = getSourceLink(article);
        if (url.isEmpty()) {
            return false;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        } catch (Exception e) {
            System.err.println("Could not open source link: " + e.getMessage());
        }
        return false;
    }
}
