package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.IndexingSettings;
import searchengine.config.Site;
import searchengine.dto.*;
import searchengine.exceptions.SearchException;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.RatingRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.LemmaFinder;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    SiteRepository siteRepository;

    LemmaRepository lemmaRepository;

    PageRepository pageRepository;

    RatingRepository ratingRepository;

    StringBuilder stringBuilder;

    private static final double THRESHOLD = 0.97;

    LemmaFinder lemmaFinder;

    {
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final IndexingSettings indexingSettings;

    List<Site> sitesToSearch;

    String[] searchWordsNormalForms;

    @Override
    public SearchResponse searchService(SearchRequest request) {
        List<PageDTO> foundPages;
        List<String> filteredLemmas;
        String message = "";
        long searchStartTime = System.nanoTime();

        if (request.getQuery() == null || request.getQuery().length() == 0) {
            return new SearchResponse(false, "Задан пустой поисковый запрос");
        }
//        Set<String> setRequestLemmas = lemmaFinder.getLemmaSet(request.getQuery());
        searchWordsNormalForms = lemmaFinder.getLemmaSet(request.getQuery()).toArray(new String[0]);
        if (searchWordsNormalForms.length == 0) {
            return new SearchResponse(false, "Не удалось выделить леммы для поиска из запроса");
        }

        filteredLemmas = filterPopularLemmasOut(sitesToSearch, List.of(searchWordsNormalForms), THRESHOLD);

        if (filteredLemmas.size() == 0) {
            return new SearchResponse(false, "По запросу '" + request.getQuery() + "' ничего не найдено");
        }
        foundPages = findRelevantPages(filteredLemmas, sitesToSearch, request.getLimit(), request.getOffset());

        if (foundPages.size() == 0) {
            return new SearchResponse(false, "По запросу '" + request.getQuery() + "' ничего не найдено");
        }
        sitesToSearch = getSitesToSearch(request.getSite());

        double maxRelevance = foundPages.get(0).getRelevance();

        List<FoundPage> searchResults = processPages(foundPages, filteredLemmas);
        return new SearchResponse(
                true,
                message + String.format(" Время поиска : %.3f сек.", (System.nanoTime() - searchStartTime) / 1000000000.),
                searchResults.size(),
                searchResults
        );
    }

    List<FoundPage> processPages(List<PageDTO> foundPages, List<String> searchQuery) {
        List<FoundPage> result = new ArrayList<>();
        for (PageDTO page : foundPages) {
            Document content = Jsoup.parse(page.getContent());
            result.add(new FoundPage(page.getPath(), content.title(), stringBuilder.getSnippet(content.title(), searchQuery), page.getRelevance()));
        }
        return result;
    }

    @Transactional
    public List<String> filterPopularLemmasOut(List<Site> sites, List<String> lemmas, double threshold) {
        return lemmaRepository.filterPopularLemmas(
                getSitesId(),
                lemmas,
                threshold).stream().map(FilteredLemmaDTO::getLemma).toList();
    }

    List<PageDTO> findRelevantPages(List<String> filteredLemmas, List<Site> sitesToSearch, int limit, int offset) {
        List<PageDTO> foundPages;
        do {
            foundPages = getSortedRelevantPageDTOs(filteredLemmas, getSitesId(), limit, offset);
            if (foundPages.size() > 0) {
                break;
            }
            filteredLemmas.remove(0);
        } while (filteredLemmas.size() > 0);

        return foundPages;
    }

    public List<PageDTO> getSortedRelevantPageDTOs(List<String> lemmas, List<Long> sites, int limit, int offset) {
        List<Long> relevantPages = new ArrayList<>();
        for (String lemma : lemmas) {
            if (relevantPages.isEmpty()) {
                relevantPages = pageRepository.getAllIdsBySiteId(sites);
            }
            relevantPages = ratingRepository.findPageIdsBySiteInAndLemmaAndPageIdsIn(sites, lemma, relevantPages);
            if (relevantPages.isEmpty()) {
                return new ArrayList<>();
            }
        }
        return pageRepository.getLimitedSortedPagesByLemmasAndPageIds(lemmas, relevantPages, limit, offset);
    }

    List<Site> getSitesToSearch(String site) {
        if (site == null) {
            return indexingSettings.getSites();
        } else {
            ArrayList<Site> sites = new ArrayList<>();
            for (Site site1 : indexingSettings.getSites()) {
                if (site1.equals(site)) {
                    sites.add(site1);
                }
            }
            return sites;
        }
    }

    private List<Long> getSitesId() {
        ArrayList<Long> siteIds = new ArrayList<>();
        for (Site site : sitesToSearch) {
            siteIds.add(siteRepository.GetSiteIdByUrl(site.getUrl()));
        }
        return siteIds;
    }


//    boolean lemmasContainAnyWordNormalForm(List<String> wordNormalForms, List<String> lemmas) {
//        List<String> lemmasWordIntersection = new ArrayList<String>(lemmas);
//        lemmasWordIntersection.retainAll(wordNormalForms);
//        return !lemmasWordIntersection.isEmpty();
//    }


    //        String finalQuery = request.getQuery();
//        if (filteredLemmas.size() < searchWordsNormalForms.length) {
//            finalQuery = correctQuery(filteredLemmas, finalQuery);
//        }

//    String correctQuery(List<String> lemmas, String originalQuery) {
//        MorphologyService ms = commonContext.getMorphologyService();
//
//        String[] splitQuery = ms.splitStringToWords(originalQuery);
//        List<String> queryList = new ArrayList<>(List.of(splitQuery));
//        List<String> wordNormalForms;
//
//        for (String word : splitQuery) {
//            wordNormalForms = ms.getNormalFormOfAWord(word.toLowerCase(Locale.ROOT));
//            if (wordNormalForms.isEmpty()) {
//                queryList.remove(word);
//                continue;
//            }
//            if (!lemmasContainAnyWordNormalForm(wordNormalForms, lemmas)) {
//                queryList.remove(word);
//            }
//        }
//        return String.join(" ", queryList);
//    }

//    private TreeMap<Long, String> filterPopularLemmas(Set<String> text) {
//        TreeMap<Long, String> lemmas = new TreeMap<>();
//        ArrayList<Long> siteIds = new ArrayList<>();
//        for (Site site : sitesToSearch) {
//            siteIds.add(siteRepository.GetSiteIdByUrl(site.getUrl()));
//        }
//        for (String lemma : text) {
//            lemmas.put(lemmaRepository.getFrequency(lemma, siteIds), lemma);
//        }
//        return lemmas;

//    }
}
