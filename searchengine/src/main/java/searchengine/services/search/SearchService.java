package searchengine.services.search;

import searchengine.dto.SearchRequest;
import searchengine.dto.SearchResponse;

public interface SearchService {
    SearchResponse searchService(SearchRequest request);
}
