package searchengine.services;

import searchengine.dto.IndexPageResponse;
import searchengine.dto.IndexingStatusResponse;
import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;

import java.io.IOException;

public interface IndexingService {
    StartIndexingResponse startIndexing() throws IOException;

    StopIndexingResponse stopIndexing();

    IndexingStatusResponse indexOnePage(String url);
}

