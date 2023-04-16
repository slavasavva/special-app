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
        if (page.getCode() != 200) {
            return;
        }
        Map<String, Integer> collectedLemmas = lemmaFinder.collectLemmas(htmlCleaningTag(page.getContent()));

        collectedLemmas.forEach((collectedLemma, rank) -> {
            Lemma lemma = lemmaRepository.findByLemmaAndSiteId(collectedLemma, page.getSiteId());
            Long lemmaId;
            if (lemma == null) {
                lemmaId = addLemma(page.getSiteId(), collectedLemma);
            } else {
                lemmaId = lemma.getId();
            }
            addRating(page.getId(), lemmaId, rank);
            lemmaRepository.increaseFrequencyByLemmaAndSiteId(collectedLemma, page.getSiteId());
        });
    }

    static String htmlCleaningTag(String html) {
        return Jsoup.clean(html, Whitelist.none());
    }

    private Long addLemma(Long siteId, String lemmaName) {
        Lemma lemma = new Lemma();
        lemma.setSiteId(siteId);
        lemma.setLemma(lemmaName);
        lemma.setFrequency(1);
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
