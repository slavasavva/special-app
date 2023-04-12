package searchengine.dto;

import lombok.Data;

@Data
public class FoundPage {
//    private String site;
//    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;

    public FoundPage(String uri, String title, String snippet, double relevance) {
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }

    @Override
    public String toString() {
        return "uri: " + this.getUri() + "\n" +
                "relevance: " + this.getRelevance() + "\n" +
                "title: " + this.getTitle() + "\n" +
                "snippet:\n" + this.getSnippet() + "\n";
    }
}
