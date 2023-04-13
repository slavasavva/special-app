package searchengine.dto;

import lombok.Data;

@Data
public class FoundPage {
    private final String site;

    private final String siteName;

    private final String uri;

    private final String title;

    private final String snippet;

    private final double relevance;
}
