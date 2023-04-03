package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.IndexingSettings;
import searchengine.dto.IndexPageResponse;
import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
    private final IndexingSettings indexingSettings;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    @Override
    public StartIndexingResponse startIndexing() {
        int configSitesSize = indexingSettings.getSites().size();
        List<Callable<Object>> indexingTasks = new ArrayList<>(configSitesSize);
        indexingSettings.getSites().forEach(configSite -> indexingTasks.add(Executors.callable(() -> indexSite(configSite))));
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
        StopIndexingResponse stopIndexingResponse = new StopIndexingResponse();
        stopIndexingResponse.setResult(true);
        return stopIndexingResponse;
    }

    @Override
    public IndexPageResponse pageIndexing() {
        IndexPageResponse indexPageResponse = new IndexPageResponse();
        indexPageResponse.setResult(true);
        return indexPageResponse;
    }
}
