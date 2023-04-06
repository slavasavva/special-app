package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.IndexingSettings;
import searchengine.dto.IndexPageResponse;
import searchengine.dto.IndexingStatusResponse;
import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;
import searchengine.exceptions.IndexingStatusException;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final IndexingSettings indexingSettings;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    WebSearchTask webSearchTask;
    @Getter
    private Thread indexingMonitorTread;

    @Override
    public StartIndexingResponse startIndexing() throws IndexingStatusException, IOException {
//        pageRepository.deleteAll();
        if (indexingMonitorTread != null) {
            throw new IndexingStatusException("Индексация уже запущена");
        }
        int configSitesSize = indexingSettings.getSites().size();
        List<Callable<Object>> indexingTasks = new ArrayList<>(configSitesSize);
        indexingSettings.getSites().forEach(configSite -> indexingTasks.add(Executors.callable(()
                -> indexSite(configSite))));
        ExecutorService executorService = Executors.newFixedThreadPool(configSitesSize);
        try {
            executorService.invokeAll(indexingTasks);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException: " + e.getMessage());
        }
        System.out.println("done");
        StartIndexingResponse startIndexingResponse = new StartIndexingResponse();
        startIndexingResponse.setResult(true);
        return startIndexingResponse;
    }

    private void indexSite(searchengine.config.Site configSite) {
        String url = configSite.getUrl();
        Long oldSiteId = getSiteIdByUrl(url);
        deleteSiteByUrl(url);
        if (oldSiteId != null) {
            deletePageBySiteId(oldSiteId);
        }
        Long siteId = addSite(configSite);
        new ForkJoinPool().invoke(new WebSearchTask(url, siteId, url, siteRepository, pageRepository));
        siteRepository.setType(url, StatusType.INDEXED.toString());
    }

    private Long getSiteIdByUrl(String url) {
        List<Site> sites = siteRepository.findByUrl(url);
        return sites.size() > 0 ? sites.get(0).getId() : null;
    }

    private void deleteSiteByUrl(String url) {
        siteRepository.deleteByUrl(url);
    }

    private void deletePageBySiteId(Long siteId) {
        pageRepository.deleteBySiteId(siteId);
    }

    private Long addSite(searchengine.config.Site configSite) {
        Site site = new Site();
        site.setType(StatusType.INDEXING);
        site.setUrl(configSite.getUrl());
        site.setName(configSite.getName());
        return siteRepository.save(site).getId();
    }

    @Override
    public StopIndexingResponse stopIndexing() {
        webSearchTask.stop = true;
        siteRepository.stopIndexing(StatusType.FAILED.toString(), StatusType.INDEXING.toString());
        StopIndexingResponse stopIndexingResponse = new StopIndexingResponse();
        stopIndexingResponse.setResult(true);
        return stopIndexingResponse;
    }

    @Override
    public IndexingStatusResponse indexOnePage(String url) {
        boolean wasNotIndexing = false;
        String mimeType = URLConnection.guessContentTypeFromName(url);
        if (mimeType != null && !mimeType.startsWith("text")) {
            throw new IndexingStatusException("Страницы с типом \"" + URLConnection.guessContentTypeFromName(url) + "\" не участвуют в индексировании");
        }

        return new IndexingStatusResponse(true, null);
    }

//    public IndexingStatusResponse indexOnePag(String url) throws IOException, IndexingStatusException {
//        boolean wasNotIndexing = false;
//        String mimeType = URLConnection.guessContentTypeFromName(url);
//
//        if (mimeType != null && !mimeType.startsWith("text")) {
//            throw new IndexingStatusException("Страницы с типом \"" + URLConnection.guessContentTypeFromName(url) + "\" не участвуют в индексировании");
//        }
//
//        sitePools = new ConcurrentHashMap<>();
//        List<Field> fields = commonContext.getDatabaseService().getAllFields();
//        List<String> userProvidedSitesUrls = userProvidedData.getSites().stream().map(SiteUrlAndNameDTO::getUrl).collect(Collectors.toList());
//
//        String siteUrl = "";
//        for (String userUrl : userProvidedSitesUrls) {
//            if (url.startsWith(userUrl)) {
//                siteUrl = userUrl;
//            }
//        }
//        if (siteUrl.isBlank()) {
//            throw new IndexingStatusException("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
//        }
//
//        if (!isIndexing()) {
//            wasNotIndexing = true;
//            commonContext.setIndexing(true);
//            commonContext.resetIndexingMessage();
//            sitePools = new ConcurrentHashMap<>();
//        }
//
//        Site site = commonContext.getDatabaseService().getSiteByUrl(siteUrl);
//        commonContext.setIndexingOnePage(true);
//        addOnePageAndIndex(site, url, fields);
//        startMonitoringThreadIfWasNotIndexing(wasNotIndexing, "Indexing-Monitor");
//        return new IndexingStatusResponse(true, null);
//    }

    public String getTopLevelUrl(String url) {
        String[] splitSite = url.split("//|/");
        return splitSite[0] + "//" + splitSite[1];
    }

//    @Override
//    public IndexPageResponse pageIndexing() {
//        IndexPageResponse indexPageResponse = new IndexPageResponse();
//        indexPageResponse.setResult(true);
//        return indexPageResponse;
//    }
}
