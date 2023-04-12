package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.IndexingStatusResponse;
import searchengine.dto.SearchRequest;
import searchengine.dto.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.search.SearchService;
import searchengine.services.StatisticsService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexService, SearchService searchService ) {
        this.statisticsService = statisticsService;
        this.indexingService = indexService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingStatusResponse> startIndexing() throws IOException {
        IndexingStatusResponse status = indexingService.startIndexing();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingStatusResponse> stopIndexing() {
        IndexingStatusResponse status = indexingService.stopIndexing();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingStatusResponse>
    indexPage(@RequestParam String url) throws IOException {
        IndexingStatusResponse status = indexingService.indexPage(url);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchService(@RequestParam(name = "query") String query,
                                                 @RequestParam(name = "site", required = false) String site,
                                                 @RequestParam(name = "offset", defaultValue = "0") int offset,
                                                 @RequestParam(name = "limit", defaultValue = "20") int limit) {

        SearchRequest request = new SearchRequest(query, site, offset, limit);
        return ResponseEntity.ok(searchService.searchService(request));
    }
}
