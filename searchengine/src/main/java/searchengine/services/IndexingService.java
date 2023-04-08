package searchengine.services;

import searchengine.dto.IndexingStatusResponse;

import java.io.IOException;

public interface IndexingService {
    IndexingStatusResponse startIndexing() throws IOException;

    IndexingStatusResponse stopIndexing();

    IndexingStatusResponse indexPage(String url);

}

