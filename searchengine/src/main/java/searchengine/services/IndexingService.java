package searchengine.services;

import searchengine.dto.IndexingStatusResponse;

public interface IndexingService {
    IndexingStatusResponse startIndexing();

    IndexingStatusResponse stopIndexing();

    IndexingStatusResponse indexPage(String url);
}
