package searchengine.dto;

public interface FilteredPage {
    String getSiteUrl();
    String getSiteName();
    String getPath();
    String getContent();
    double getRelevance();
}
