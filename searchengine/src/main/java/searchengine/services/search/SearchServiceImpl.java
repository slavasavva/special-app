package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.IndexingSettings;
import searchengine.config.Site;
import searchengine.dto.*;
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
    private final SiteRepository siteRepository;

    private final LemmaRepository lemmaRepository;

    private final PageRepository pageRepository;

    private final RatingRepository ratingRepository;

    private MakeSnippet stringBuilder = new MakeSnippet();

    private static final double THRESHOLD = 0.97;

    private LemmaFinder lemmaFinder;

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
        List<FilteredPage> foundPages;
        List<String> filteredLemmas;
        String message = "";

        if (request.getQuery() == null || request.getQuery().length() == 0) {
            return new SearchResponse(false, "Задан пустой поисковый запрос");
        }
        searchWordsNormalForms = lemmaFinder.getLemmaSet(request.getQuery()).toArray(new String[0]);
        if (searchWordsNormalForms.length == 0) {
            return new SearchResponse(false, "Не удалось выделить леммы для поиска из запроса");
        }
        sitesToSearch = getSitesToSearch(request.getSite());

        filteredLemmas = filterPopularLemmasOut(List.of(searchWordsNormalForms), THRESHOLD);

        if (filteredLemmas.size() == 0) {
            return new SearchResponse(false, "По запросу '" + request.getQuery() + "' ничего не найдено");
        }
        foundPages = getSortedRelevantPageDTOs(filteredLemmas, getSitesId(), request.getLimit(), request.getOffset());

        if (foundPages.size() == 0) {
            return new SearchResponse(false, "По запросу '" + request.getQuery() + "' ничего не найдено");
        }
        int count = getSortedRelevantPageDTOs(filteredLemmas, getSitesId(), 20, 0).size();
        double maxRelevance = foundPages.get(0).getRelevance();
        List<FoundPage> searchResults = processPages(foundPages, filteredLemmas, maxRelevance);
        return new SearchResponse(
                true,
                message,
                count,
                searchResults
        );
    }

    private List<FoundPage> processPages(List<FilteredPage> foundPages, List<String> searchQuery, double maxRelevance) {
        List<FoundPage> result = new ArrayList<>();
        for (FilteredPage page : foundPages) {
            Document content = Jsoup.parse(page.getContent());
            result.add(new FoundPage(page.getSiteUrl(), page.getSiteName(), page.getPath(), content.title(),
                    stringBuilder.getSnippet(content.text(), searchQuery), page.getRelevance() / maxRelevance));
        }

        return result;
    }

    @Transactional
    public List<String> filterPopularLemmasOut(List<String> lemmas, double threshold) {
        List<String> filteredLemmas = new ArrayList<>();
        List<FilteredLemma> filteredLemmaDTOS = lemmaRepository.filterPopularLemmas(
                getSitesId(),
                lemmas,
                threshold);
        for (FilteredLemma lemmaDTO : filteredLemmaDTOS) {
            filteredLemmas.add(lemmaDTO.getLemma());
        }
        return filteredLemmas;
    }

    public List<FilteredPage> getSortedRelevantPageDTOs(List<String> lemmas, List<Long> sites, int limit, int offset) {
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
                if (site1.getUrl().equals(site)) {
                    sites.add(site1);
                }
            }
            return sites;
        }
    }

    private List<Long> getSitesId() {
        ArrayList<Long> siteIds = new ArrayList<>();
        for (Site site : sitesToSearch) {
            siteIds.add(siteRepository.getSiteIdByUrl(site.getUrl()));
        }
        return siteIds;
    }
}
