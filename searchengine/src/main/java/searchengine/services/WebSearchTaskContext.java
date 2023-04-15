package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.IndexingSettings;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSearchTaskContext {

    private String startUrl;

    private Long siteId;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private IndexingPage indexingPage;

    private IndexingSettings indexingSettings;



    private final AtomicBoolean stop;

    public WebSearchTaskContext(String startUrl, Long siteId,
                                SiteRepository siteRepository, PageRepository pageRepository,
                                IndexingPage indexingPage, IndexingSettings indexingSettings, AtomicBoolean stop) {
        this.startUrl = startUrl;
        this.siteId = siteId;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.indexingPage = indexingPage;
        this.indexingSettings = indexingSettings;
        this.stop = stop;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public Long getSiteId() {
        return siteId;
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

    public IndexingSettings getIndexingSettings() {
        return indexingSettings;
    }
}
