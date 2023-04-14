package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.RatingRepository;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexingPageImpl implements IndexingPage {

    private final RatingRepository ratingRepository;

    private final LemmaRepository lemmaRepository;

    LemmaFinder lemmaFinder;

    {
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void indexingPage(Page page) {
        if (page.getCode() == 200) {
            Map<String, Integer> lemmas = lemmaFinder.collectLemmas(htmlCleaningTag(page.getContent()));
            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                int uniqueLemmas = lemmaRepository.checkLemmaPresence(entry.getKey(), page.getSiteId());
                if (uniqueLemmas == 0) {
                    Long lemmaId = addLemma(page.getSiteId(), entry.getKey(), 1);
                    addRating(page.getId(), lemmaId, entry.getValue());
                } else {
                    lemmaRepository.setFrequencyByLemmaAndSiteId(entry.getKey(), page.getSiteId());
                }
            }
        }
   }

    static String htmlCleaningTag(String html) {
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
