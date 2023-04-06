package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.RecursiveAction;

public class WebSearchTask extends RecursiveAction {
    private IndexingServiceImpl indexService;
    private String startUrl;

    private String url;

    private Long siteId;

    private SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private List<WebSearchTask> subTasks = new LinkedList<>();

    private LocalDate localDate = LocalDate.now();

    public WebSearchTask(String startUrl, Long siteId, String url,
                         SiteRepository siteRepository, PageRepository pageRepository) {
        this.startUrl = startUrl;
        this.siteId = siteId;
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }
    public boolean stop = false;

    @Override
    protected void compute() {
        int allPaths = pageRepository.findAll().size();
        if (allPaths > 10) {
            return;
        }
        if (wrongLink(siteId, url)) {
            return;
        }
        if (stop) {
            System.out.println("СТООООП!");
            return;
        }
        System.out.println("(" + allPaths + ") processing URL " + url);
        try {
            Thread.sleep(500);
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            addPage(siteId, url, 200, document.html());
            Elements linkElements = document.select("a[href]");
            for (Element linkElement : linkElements) {
                String link = linkElement.attr("abs:href");
                if (!wrongLink(siteId, link)) {
                    WebSearchTask webSearchTask = new WebSearchTask(startUrl, siteId, link,
                            siteRepository, pageRepository);
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
            addPage(siteId, url, 500, e.getMessage());
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

    private void addPage(Long siteId, String path, int code, String content) {
        synchronized (pageRepository) {
            if (!wrongLink(siteId, path)) {
                try {
                    createPage(siteId, path, code, content);
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }

    private void createPage(Long siteId, String path, int code, String content) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        pageRepository.save(page);
    }
}
