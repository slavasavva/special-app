package searchengine.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.IndexPageResponse;
import searchengine.dto.IndexingStatusResponse;
import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.exceptions.UnknownIndexingStatusException;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

//    @GetMapping("/startIndexing")
//    public ResponseEntity<StartIndexingResponse> startIndexing() {
//        return ResponseEntity.ok(indexingService.startIndexing());
//    }

    @GetMapping("/startIndexing")
    public ResponseEntity<StartIndexingResponse> startIndexing() throws IOException {
        StartIndexingResponse status = indexingService.startIndexing();
            return ResponseEntity.ok(status);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<StopIndexingResponse> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingStatusResponse>
    indexPage(@RequestParam String url) throws IOException {
        IndexingStatusResponse status = indexingService.indexOnePage(url);

            return ResponseEntity.ok(status);
    }

    @GetMapping("/search")
    public ResponseEntity<StopIndexingResponse> search() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }
}
