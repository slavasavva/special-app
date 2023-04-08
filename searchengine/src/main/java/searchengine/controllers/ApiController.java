package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.IndexingStatusResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.StatisticsService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexService, IndexingServiceImpl indexingServiceImpl) {
        this.statisticsService = statisticsService;
        this.indexingService = indexService;
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

//    @PostMapping("/indexPage")
//    public ResponseEntity<IndexingStatusResponse>
//    indexPage(@RequestParam String url) throws IOException {
//        IndexingStatusResponse status = indexingService.indexOnePage(url);
//        if (indexingService.isIndexing()) {
//            return ResponseEntity.ok(status);
//        }
//        throw new UnknownIndexingStatusException("Неизвестная ошибка индексирования");
//    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingStatusResponse>
    indexPage(@RequestParam String url) throws IOException {
        IndexingStatusResponse status = indexingService.indexPage(url);
        return ResponseEntity.ok(status);
    }

//    @GetMapping("/search")
//    public ResponseEntity<StopIndexingResponse> search() {
//        return ResponseEntity.ok(indexingService.stopIndexing());
//    }
}
