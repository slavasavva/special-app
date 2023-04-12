package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.IndexingSettings;
import searchengine.dto.IndexingStatusResponse;
import searchengine.exceptions.IndexingStatusException;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.RatingRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final IndexingPageImpl indexingPage;

    private final IndexingSettings indexingSettings;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final RatingRepository ratingRepository;

    private final LemmaRepository lemmaRepository;

    private boolean indexing;

    private int threadCount;

    private final AtomicBoolean stop = new AtomicBoolean();

    @Override

    public IndexingStatusResponse startIndexing() throws IndexingStatusException {
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
        }
        Long siteId = addSite(configSite.getUrl(), configSite.getName());
        WebSearchTaskContext webSearchTaskContext = new WebSearchTaskContext(url, siteId,
                siteRepository, pageRepository, indexingPage, stop);
        new ForkJoinPool().invoke(new WebSearchTask(url, webSearchTaskContext));
        setSiteStatusType(url, siteId);
        done();
    }

    private void setSiteStatusType(String url, Long siteId) {
        if (stop.get()) {
            siteRepository.setType(url, StatusType.FAILED.toString());
            siteRepository.setLastError(url, "Индексация прервана пользователем");
//            siteRepository.setTypeAndLastError(url, StatusType.FAILED.toString(), "Индексация прервана пользователем");

        } else if (pageRepository.countIndexedPage(siteId) == 0) {
            siteRepository.setType(url, StatusType.FAILED.toString());
            siteRepository.setLastError(url, "Сайт недоступен длч индексации");
//            siteRepository.setTypeAndLastError(url, StatusType.FAILED.toString(), "Сайт недоступен длч индексации");
        } else if (pageRepository.countIndexedPage(siteId) != 0 &&
                pageRepository.countSuccessfulIndexedPage(siteId) / pageRepository.countIndexedPage(siteId) <
                        indexingSettings.getSiteIndexingSuccessfulPercentage()) {
            siteRepository.setLastError(url, "Проиндексировано менее 70 % страниц сайта");
            siteRepository.setType(url, StatusType.FAILED.toString());
//            siteRepository.setTypeAndLastError(url, StatusType.FAILED.toString(), "Проиндексировано менее 70 % страниц сайта");
        } else {
            siteRepository.setType(url, StatusType.INDEXED.toString());
        }
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

    private Long addSite(String url, String name) {
        Site site = new Site();
        site.setType(StatusType.INDEXING);
        site.setUrl(url);
        site.setName(name);
        return siteRepository.save(site).getId();
    }

    private Page addPage(Long siteId, String path, int code, String content) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(path);
        page.setCode(200);
        page.setContent(content);
        pageRepository.save(page);
        return page;
    }

    @Override
    public IndexingStatusResponse stopIndexing() throws IndexingStatusException {
        if (!indexing) {
            return new IndexingStatusResponse(false, "Индексация не запущена");
//            throw new IndexingStatusException("Индексация не запущена");
        }
        stop.set(true);
//        IndexingStatusResponse stopIndexingResponse = new IndexingStatusResponse();
//        stopIndexingResponse.setResult(true);
        return new IndexingStatusResponse(true, null);
    }

    @Override
    public IndexingStatusResponse indexPage(String url) {
        if (!findSiteByPageUrl(url)) {
            return new IndexingStatusResponse(false, "Данная страница находится за пределами сайтов," +
                    " указанных в конфигурационном файле");
        }
        String path = deleteTopLevelUrl(url);
        Long pageId = pageRepository.getPageIdByPath(path);
        Long siteId = siteRepository.GetSiteIdByUrl(getTopLevelUrl(url));
        String content = getHtmlFromUrl(url);
        if (pageId != null){
            List<Long> lemmasId = ratingRepository.getLemmasIgByPageId(pageId);
            for (Long lemmaId : lemmasId) {
                lemmaRepository.deleteById(lemmaId);
            }
            pageRepository.deleteById(pageId);
            ratingRepository.deleteByPageId(pageId);
        }
            Page page = addPage(siteId, path, 200, content);
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

    public String deleteTopLevelUrl(String url) {
        String[] splitSite = url.split("//|/");
        return url.replace((splitSite[0] + "//" + splitSite[1]), "");
    }

    public String getTopLevelUrl(String url) {
        String[] splitSite = url.split("//|/");
        return splitSite[0] + "//" + splitSite[1];
    }

    public String getSiteName(String url) {
        String[] splitSite = url.split("//|/");
        return splitSite[1];
    }

//    @Override
//    public IndexPageResponse pageIndexing() {
//        IndexPageResponse indexPageResponse = new IndexPageResponse();
//        indexPageResponse.setResult(true);
//        return indexPageResponse;
//    }
}
