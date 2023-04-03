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
import java.util.concurrent.RecursiveAction;

public class WebSearchTask extends RecursiveAction {
    private String startUrl;

    private String url;

    private Long siteId;

    private SiteRepository siteRepository;

    private PageRepository pageRepository;

    private List<WebSearchTask> subTasks = new LinkedList<>();

    LocalDate localDate = LocalDate.now();

    public WebSearchTask(String startUrl, Long siteId, String url,
                         SiteRepository siteRepository, PageRepository pageRepository) {
        this.startUrl = startUrl;
        this.siteId = siteId;
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    protected void compute() {
//        if (pageRepository.findBySiteIdAndPath(siteId, url).size() > 100) {
//            return;
//        }
//        if (wrongLink(siteId, url)) {
//            return;
//        }
        System.out.println("processing url = " + url);
        try {
            Thread.sleep(500);
//            Document document = Jsoup.connect(url).get();
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            addPage(siteId, url, 200, document.html()); // TODO document.?
            Elements linkElements = document.select("a[href]");
            for (Element linkElement : linkElements) {
                String link = linkElement.attr("abs:href");
//                if (!wrongLink(siteId, link)) {
                WebSearchTask webSearchTask = new WebSearchTask(startUrl, siteId, link,
                        siteRepository, pageRepository);
                webSearchTask.fork();
                subTasks.add(webSearchTask);
//                }
            }
            for (WebSearchTask webSearchTask : subTasks) {
                webSearchTask.join();
            }
        } catch (Exception e) {
            addPage(siteId, url, 500, ""); // TODO code?
            System.err.println("Exception for '" + url + "': " + e.getMessage());
        }
    }

    private boolean wrongLink(Long siteId, String link) {
        return pageRepository.findBySiteIdAndPath(siteId, link).size() > 0
                || !link.startsWith(startUrl)
                || link.contains("#")
                || link.endsWith("pdf")
                || link.endsWith("xls")
                || link.endsWith("doc")
                ;
    }

    private void addPage(Long siteId, String path, int code, String content) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        pageRepository.save(page);
    }
}
