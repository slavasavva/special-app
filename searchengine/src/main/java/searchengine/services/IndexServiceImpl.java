package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.IndexPageResponse;
import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SitesList configSites;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    @Override
    public StartIndexingResponse startIndexing() {
        configSites.getSites().forEach(configSite -> {
            String url = configSite.getUrl();
            Long oldSiteId = getSiteIdByUrl(url);
            deleteSiteByUrl(url);
            if (oldSiteId != null) {
                deletePageBySiteId(oldSiteId);
            }
            Long siteId = addSite(configSite);
            new ForkJoinPool().invoke(new WebSearchTask(url, siteId, url, siteRepository, pageRepository));
        });

        System.out.println("done");
        StartIndexingResponse startIndexingResponse = new StartIndexingResponse();
        startIndexingResponse.setResult(true);
        return startIndexingResponse;
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
