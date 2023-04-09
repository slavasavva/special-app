package searchengine.services;

import searchengine.model.Page;

import java.util.Map;

public class IndexingPage {

    LemmaFinder lemmaFinder;

    private Page page;

    public IndexingPage(Page page) {
        this.page = page;
    }
    public void indexingPage(Page page){
        Map<String, Integer> lemmas = lemmaFinder.StripHtml(page.getContent());


    }
}
