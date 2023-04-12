//package searchengine.services.search;
//
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import searchengine.dto.FoundPage;
//import searchengine.dto.PageDTO;
//import searchengine.services.SnippetBuilder;
//
//import java.util.List;
//
//public class PageDtoAdapter extends FoundPage {
//    public PageDtoAdapter(PageDTO page, double maxRelevance, List<String> searchQuery) {
//
//        Document content = Jsoup.parse(page.getContent());
//
//        this.setSite(page.getSiteUrl());
//        this.setSiteName(page.getSiteName());
//        this.setUri(page.getPath());
//        this.setTitle(content.select("title").text());
//        this.setSnippet(SnippetBuilder.getSnippetFromPage(content.select("body").text(), searchQuery));
//        this.setRelevance(page.getRelevance() / maxRelevance);
//    }
//}
