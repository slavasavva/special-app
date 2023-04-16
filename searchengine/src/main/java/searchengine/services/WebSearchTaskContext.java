package searchengine.services;

import searchengine.config.IndexingSettings;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.concurrent.atomic.AtomicBoolean;

public class WebSearchTaskContext {
    private String startUrl;

    private Long siteId;

    private IndexingSettings indexingSettings;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private IndexingPage indexingPage;

    private final AtomicBoolean stop;

    public WebSearchTaskContext(String startUrl, Long siteId, IndexingSettings indexingSettings,
                                SiteRepository siteRepository, PageRepository pageRepository,
                                IndexingPage indexingPage, AtomicBoolean stop) {
        this.startUrl = startUrl;
        this.siteId = siteId;
        this.indexingSettings = indexingSettings;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.indexingPage = indexingPage;
        this.stop = stop;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public Long getSiteId() {
        return siteId;
    }

    public IndexingSettings getIndexingSettings() {
        return indexingSettings;
    }

    public SiteRepository getSiteRepository() {
        return siteRepository;
    }

    public PageRepository getPageRepository() {
        return pageRepository;
    }

    public IndexingPage getIndexingPage() {
        return indexingPage;
    }

    public AtomicBoolean getStop() {
        return stop;
    }
}
