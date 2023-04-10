package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Rating;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.RatingRepository;

import java.io.IOException;
import java.util.Map;

public class IndexingPage {

    LemmaRepository lemmaRepository;

    RatingRepository ratingRepository;

    static PageRepository pageRepository;

    static LemmaFinder lemmaFinder;

    static {
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Page page;

    public IndexingPage(Page page) {
        this.page = page;
    }

    public static void main(String[] args) throws IOException {


        String html = getHtmlFromUrl("https://www.playback.ru");
        System.out.println(html);
        System.out.println(htmlCliningTag(html));
        Map<String, Integer> savva = lemmaFinder.collectLemmas(htmlCliningTag(html));
//        Map<String, Integer> savva = lemmaFinder.collectLemmas("Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.");
       for (Map.Entry<String, Integer> entry : savva.entrySet()){
          System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    private static String getHtmlFromUrl(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void indexingPage(Page page) {
        int frequency = 1;
        if (page.getCode() == 200) {
            Map<String, Integer> lemmas = lemmaFinder.collectLemmas(htmlCliningTag(page.getContent()));
            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                int uniqueLemmas = lemmaRepository.checkLemmaContaint(entry.getKey());
                if (uniqueLemmas > 0) {
                    frequency++;
                }
                Long lemmaId = addLemma(page.getSiteId(), entry.getKey(), frequency);
                addRating(page.getId(), lemmaId, entry.getValue());
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
        }
    }

    static String htmlCliningTag(String html){
        return Jsoup.clean(html, Whitelist.none());
    }

    private Long addLemma(Long siteId, String lemmaName, int frequency) {
        Lemma lemma = new Lemma();
        lemma.setSiteId(siteId);
        lemma.setLemma(lemmaName);
        lemma.setFrequency(frequency);
        return lemmaRepository.save(lemma).getId();
    }

    private void addRating(Long pageId, Long lemmaId, int rank) {
        Rating rating = new Rating();
        rating.setPageId(pageId);
        rating.setLemmaId(lemmaId);
        rating.setRating(rank);
        ratingRepository.save(rating);
    }
}
