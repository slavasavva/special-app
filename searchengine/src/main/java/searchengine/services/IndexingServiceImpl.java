package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.IndexingSettings;
import searchengine.dto.IndexingStatusResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.RatingRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

import static searchengine.Utils.*;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final IndexingSettings indexingSettings;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final RatingRepository ratingRepository;

    private final LemmaRepository lemmaRepository;

    private final IndexingPage indexingPage;

    private boolean indexing;

    private int threadCount;

    private final AtomicBoolean stop = new AtomicBoolean();

    @Override
    public IndexingStatusResponse startIndexing() {
        if (indexing) {
            return new IndexingStatusResponse(false, "Индексация уже запущена");
        }
        indexing = true;
        indexingSettings.getSites().forEach(configSite -> new Thread(() -> indexSite(configSite)).start());
        return new IndexingStatusResponse(true, null);
    }

    private void indexSite(searchengine.config.Site configSite) {
        String url = configSite.getUrl();
        Long oldSiteId = getSiteIdByUrl(url);
        deleteSiteByUrl(url);
        if (oldSiteId != null) {
            deletePageBySiteId(oldSiteId);
            deleteLemmasBySiteId(oldSiteId);
            deleteRatingBySiteId(oldSiteId);
        }
        Long siteId = addSite(configSite.getUrl(), configSite.getName());
        WebSearchTaskContext webSearchTaskContext = new WebSearchTaskContext(url, siteId,
                indexingSettings, siteRepository, pageRepository, indexingPage, stop);
        new ForkJoinPool().invoke(new WebSearchTask(url, webSearchTaskContext));
        setSiteStatusType(url, siteId);
        done();
    }

    private synchronized void done() {
        threadCount++;
        if (threadCount == indexingSettings.getSites().size()) {
            indexing = false;
            threadCount = 0;
            stop.set(false);
            System.out.println("done");
        }
    }

    private void setSiteStatusType(String url, Long siteId) {
        if (stop.get()) {
            siteRepository.setType(url, StatusType.FAILED.name());
            siteRepository.setLastError(url, "Индексация прервана пользователем");
        } else if (pageRepository.countIndexedPage(siteId) == 0) {
            siteRepository.setType(url, StatusType.FAILED.name());
            siteRepository.setLastError(url, "Сайт недоступен для индексации");
        } else if (pageRepository.countIndexedPage(siteId) != 0
                && (double) pageRepository.countSuccessfulIndexedPage(siteId) / pageRepository.countIndexedPage(siteId) <
                indexingSettings.getSiteIndexingSuccessfulPercentage()) {
            siteRepository.setType(url, StatusType.FAILED.name());
            siteRepository.setLastError(url, "Проиндексировано менее " +
                    indexingSettings.getSiteIndexingSuccessfulPercentage() * 100 + "% страниц сайта");
        } else {
            siteRepository.setType(url, StatusType.INDEXED.name());
        }
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

    private void deleteLemmasBySiteId(Long siteId) {
        lemmaRepository.deleteBySiteId(siteId);
    }

    private void deleteRatingBySiteId(Long siteId) {
        ratingRepository.deleteBySiteId(siteId);
    }

    private Long addSite(String url, String name) {
        Site site = new Site();
        site.setType(StatusType.INDEXING);
        site.setUrl(url);
        site.setName(name);
        return siteRepository.save(site).getId();
    }

    @Override
    public IndexingStatusResponse stopIndexing() {
        if (!indexing) {
            return new IndexingStatusResponse(false, "Индексация не запущена");
        }
        stop.set(true);
        return new IndexingStatusResponse(true, null);
    }

    @Override
    public IndexingStatusResponse indexPage(String url) {
        if (!findSiteByPageUrl(url)) {
            return new IndexingStatusResponse(false, "Данная страница находится за пределами сайтов," +
                    " указанных в конфигурационном файле");
        }
        String path = getPathFromUrl(url);
        Long pageId = pageRepository.getPageIdByPath(path);
        Long siteId = siteRepository.getSiteIdByUrl(getSiteUrlFromPathUrl(url));
        String content = getHtmlFromUrl(url);
        if (pageId != null) {
            List<Long> lemmasId = ratingRepository.getLemmasIdByPageId(pageId);
            for (Long lemmaId : lemmasId) {
                lemmaRepository.deleteById(lemmaId);
            }
            pageRepository.deletePageById(pageId);
            ratingRepository.deleteByPageId(pageId);
        }
        Page page = createPage(pageRepository, siteId, path, 200, content);
        indexingPage.indexingPage(page);

        return new IndexingStatusResponse(true, null);
    }

    private static String getHtmlFromUrl(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean findSiteByPageUrl(String url) {
        for (searchengine.config.Site site : indexingSettings.getSites()) {
            if (url.startsWith(site.getUrl())) {
                return true;
            }
        }
        return false;
    }
}
