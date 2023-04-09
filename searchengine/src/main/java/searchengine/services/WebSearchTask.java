package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSearchTask extends RecursiveAction {
    private IndexingPage indexingPage;
    private String startUrl;

    private String url;

    private Long siteId;

    private SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private List<WebSearchTask> subTasks = new LinkedList<>();

    private Date date = new Date(System.currentTimeMillis());

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private AtomicBoolean stop;

    public WebSearchTask(String startUrl, Long siteId, String url,
                         SiteRepository siteRepository, PageRepository pageRepository,
                         AtomicBoolean stop) {
        this.startUrl = startUrl;
        this.siteId = siteId;
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.stop = stop;
    }

    @Override
    protected void compute() {
        int allPaths = pageRepository.findAll().size();
        if (allPaths > 20) {
            return;
        }
        if (wrongLink(siteId, url)) {
            return;
        }
//       if (Objects.equals(indexingService.deleteTopLevelUrl(url), "/")) {
//            return;
//        }
        if (stop.get()) {
            System.out.println("(" + allPaths + ") stopping URL " + url);
            return;
        }
        System.out.println("(" + allPaths + ") processing URL " + url);
        try {
            Thread.sleep(500);
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            processPage(siteId, url, 200, document.html());
            Elements linkElements = document.select("a[href]");
            for (Element linkElement : linkElements) {
                String link = linkElement.attr("abs:href");
                if (!wrongLink(siteId, link)) {
                    WebSearchTask webSearchTask = new WebSearchTask(startUrl, siteId, link,
                            siteRepository, pageRepository, stop);
                    webSearchTask.fork();
                    if (subTasks.size() < 10) {
                        subTasks.add(webSearchTask);
                    }
                }
            }
            for (WebSearchTask webSearchTask : subTasks) {
                webSearchTask.join();
            }
        } catch (Exception e) {
            processPage(siteId, url, 500, e.getMessage());
            System.err.println("Exception for '" + url + "': " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WebSearchTask.class.getSimpleName() + "[", "]")
                .add("startUrl='" + startUrl + "'")
                .add("url='" + url + "'")
                .add("siteId=" + siteId)
                .toString();
    }

    private boolean wrongLink(Long siteId, String link) {
        int sitePaths = pageRepository.findBySiteIdAndPath(siteId, link).size();
        boolean wrongLink = sitePaths > 0
                || !link.startsWith(startUrl)
                || link.contains("#")
                || link.endsWith("doc")
                || link.endsWith("gif")
                || link.endsWith("jpg")
                || link.endsWith("pdf")
                || link.endsWith("png")
                || link.endsWith("xls");
        return wrongLink;
    }

    private void processPage(Long siteId, String path, int code, String content) {
        synchronized (pageRepository) {
            if (!wrongLink(siteId, path)) {
                try {
//                    indexingService.addPage(siteId, indexingService.deleteTopLevelUrl(path), code, content);
                    createPage(siteId, deleteTopLevelUrl(path), code, content);
                    siteRepository.statusTime(url, formatter.format(date));

//TODO page to lemma
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }

    public String deleteTopLevelUrl(String url) {
        String[] splitSite = url.split("//|/");
        return url.replace((splitSite[0] + "//" + splitSite[1]), "");
    }

    public void createPage(Long siteId, String path, int code, String content) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        pageRepository.save(page);
        indexingPage.indexingPage(page);
    }
}
