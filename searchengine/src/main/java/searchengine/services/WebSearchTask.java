package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

public class WebSearchTask extends RecursiveAction {
    private String startUrl;

    private String url;

    private Long siteId;

    private SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private IndexingPage indexingPage;

    private IndexingService indexingService;

    private List<WebSearchTask> subTasks = new LinkedList<>();

    private Date date = new Date(System.currentTimeMillis());

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private AtomicBoolean stop;

    private WebSearchTaskContext webSearchTaskContext;

    public WebSearchTask(String url, WebSearchTaskContext webSearchTaskContext) {
        this.url = url;
        this.startUrl = webSearchTaskContext.getStartUrl();
        this.siteId = webSearchTaskContext.getSiteId();
        this.siteRepository = webSearchTaskContext.getSiteRepository();
        this.pageRepository = webSearchTaskContext.getPageRepository();
        this.indexingPage = webSearchTaskContext.getIndexingPage();
        this.stop = webSearchTaskContext.getStop();
        this.webSearchTaskContext = webSearchTaskContext;
    }

    @Override
    protected void compute() {
        siteRepository.statusTime(url, formatter.format(date));
        int allPaths = pageRepository.findAll().size();
        if (allPaths > 5) {
            return;
        }
        if (wrongLink(siteId, url)) {
            return;
        }
        if (!url.startsWith(startUrl.replace("www.", "")) &&
                !url.startsWith(startUrl)) {
            return;
        }
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
                    WebSearchTask webSearchTask = new WebSearchTask(link, webSearchTaskContext);
                    webSearchTask.fork();
                    subTasks.add(webSearchTask);
//                    if (subTasks.size() < 10) {
//                        subTasks.add(webSearchTask);
//                    }
                }
            }
            for (WebSearchTask webSearchTask : subTasks) {
                webSearchTask.join();
            }
        } catch (Exception e) {
            processPage(siteId, url, 500, e.getMessage());
            siteRepository.statusTime(url, formatter.format(date));
            System.err.println("Exception for '" + url + "': " + e.getMessage());
            e.printStackTrace();
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
        int sitePaths = pageRepository.findBySiteIdAndPath(siteId, deleteTopLevelUrl(link)).size();
        boolean wrongLink = sitePaths > 0
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
//                    Page page = indexingService.addPage(siteId, deleteTopLevelUrl(path), code, content);
                    Page page = createPage(siteId, deleteTopLevelUrl(path), code, content);
                    if (page.getCode() == 200) {
                        indexingPage.indexingPage(page);
                    }
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        }
    }

    private String deleteTopLevelUrl(String url) {
        if (url.equals("")) {
            return url;
        }
        String[] splitSite = url.split("//|/");
        return url.replace((splitSite[0] + "//" + splitSite[1]), "");
    }


    public Page createPage(Long siteId, String path, int code, String content) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        pageRepository.save(page);
        return page;
    }
}
