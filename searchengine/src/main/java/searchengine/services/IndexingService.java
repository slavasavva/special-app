package searchengine.services;

import searchengine.dto.IndexingStatusResponse;
import searchengine.model.Page;

public interface IndexingService {
    IndexingStatusResponse startIndexing();

    IndexingStatusResponse stopIndexing();

    IndexingStatusResponse indexPage(String url);

    Page addPage(Long siteId, String path, int code, String content);
}
