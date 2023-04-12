package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.IndexingSettings;
import searchengine.config.Site;
import searchengine.dto.*;
import searchengine.exceptions.IndexingStatusException;
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
    LemmaFinder lemmaFinder;

    PageRepository pageRepository;

    RatingRepository ratingRepository;

    private static final double THRESHOLD = 0.97;

    {
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final IndexingSettings indexingSettings;

    public static void main(String[] args) throws IOException {
        LemmaFinder lemmaFinder1 = LemmaFinder.getInstance();
        Map<String, Integer> lem = lemmaFinder1.collectLemmas("леопарды в горах алтая");
        Set<String> le = lemmaFinder1.getLemmaSet("Выхожу один я на дорогу");
        for (Map.Entry<String, Integer> entry : lem.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println(le);
    }

    List<Site> sitesToSearch;
    String[] searchWordsNormalForms;

    @Override
    public SearchResponse searchService(SearchRequest request) throws IndexingStatusException {
        List<PageDTO> foundPages;
        List<String> filteredLemmas;
        String message = "";
        long searchStartTime = System.nanoTime();

        if (request.getQuery() == null) {
            return new SearchResponse(false, "Задан пустой поисковый запрос", 0, null);
        }
        if (searchWordsNormalForms.length == 0) {
            throw new SearchException("Не удалось выделить леммы для поиска из запроса");
        }

        filteredLemmas = filterPopularLemmasOut(sitesToSearch,
                List.of(searchWordsNormalForms), THRESHOLD);

        if (filteredLemmas.size() == 0) {
            throw new SearchException("По запросу '" + request.getQuery() + "' ничего не найдено");
        }
        foundPages = findRelevantPages(filteredLemmas, sitesToSearch, request.getLimit(), request.getOffset());

        if (foundPages.size() == 0) {
            throw new SearchException("По запросу '" + request.getQuery() + "' ничего не найдено");
        }
        sitesToSearch = getSitesToSearch(request.getSite());
        Set<String> setRequestLemmas = lemmaFinder.getLemmaSet(request.getQuery());
        searchWordsNormalForms = (String[]) setRequestLemmas.toArray();
        double maxRelevance = foundPages.get(0).getRelevance();


//        List<FoundPage> searchResults = processPages(foundPages);
//        return new SearchResponse(
//                true,
//                message + String.format(" Время поиска : %.3f сек.", (System.nanoTime() - searchStartTime)/1000000000.),
//                searchResults.size(),
//                searchResults
//        );


           return null;
        }

//    List<FoundPage> processPages(List<PageDTO> foundPages) {
//        List<FoundPage> result = new ArrayList<>();
//        for (PageDTO page : foundPages) {
//            Document content = Jsoup.parse(page.getContent());
//            result.add(new FoundPage(page.getPath(), content.title(), snippet, page.getRelevance()));
//        }
//        return null;
//    }


    boolean lemmasContainAnyWordNormalForm(List<String> wordNormalForms, List<String> lemmas) {
        List<String> lemmasWordIntersection = new ArrayList<String>(lemmas);
        lemmasWordIntersection.retainAll(wordNormalForms);
        return !lemmasWordIntersection.isEmpty();
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
            for (Site site1 : indexingSettings.getSites())
                if (site1.equals(site)) {
                    sites.add(site1);
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
