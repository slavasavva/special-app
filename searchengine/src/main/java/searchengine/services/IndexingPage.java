package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Rating;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.RatingRepository;

import java.util.Map;

public class IndexingPage {

    LemmaRepository lemmaRepository;

    RatingRepository ratingRepository;

    LemmaFinder lemmaFinder;

    private Page page;

    public IndexingPage(Page page) {
        this.page = page;
    }

    public static void main(String[] args) {

    }

    public void indexingPage(Page page) {
        int frequency = 1;
        if (page.getCode() == 200) {
            Map<String, Integer> lemmas = lemmaFinder.StripHtml(page.getContent());
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
