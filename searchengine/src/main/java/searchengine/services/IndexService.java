package searchengine.services;

import searchengine.dto.IndexPageResponse;
import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;

public interface IndexService {
    StartIndexingResponse startIndexing();

    StopIndexingResponse stopIndexing();

    IndexPageResponse pageIndexing();
}
