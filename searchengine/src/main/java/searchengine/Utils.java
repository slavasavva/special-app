package searchengine;

import searchengine.model.Page;
import searchengine.repositories.PageRepository;

public class Utils {
    public static Page createPage(PageRepository pageRepository, Long siteId, String path, int code, String content) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        pageRepository.save(page);
        return page;
    }

    public static String getPathFromUrl(String url) {
        String[] splitSite = url.split("//|/");
        if (splitSite.length < 2) {
            return "";
        }
        return url.replace((splitSite[0] + "//" + splitSite[1]), "");
    }

    public static String getSiteUrlFromPathUrl(String url) {
        String[] splitSite = url.split("//|/");
        return splitSite[0] + "//" + splitSite[1];
    }
}
