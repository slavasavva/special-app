package searchengine.dto;

import lombok.Data;

@Data
public class FoundPage {
    private final String url;

    private final String title;

    private final String snippet;

    private final double relevance;
}
