package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.IndexingSettings;
import searchengine.config.Site;
import searchengine.dto.PageDTO;
import searchengine.dto.SearchRequest;
import searchengine.dto.SearchResponse;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    LemmaFinder lemmaFinder;

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
            System.out.println(le);
        }
    }

    @Override
    public SearchResponse searchService(SearchRequest request) {

        String[] searchWordsNormalForms;
        List<Site> sitesToSearch;
        List<PageDTO> foundPages;
        List<String> filteredLemmas;
        String message = "";
        long searchStartTime = System.nanoTime();
        sitesToSearch = getSitesToSearch(request.getSite());
        Set<String> setRequestLemmas = lemmaFinder.getLemmaSet(request.getQuery());
        Map<String, Integer> mapRequestLemmas = lemmaFinder.collectLemmas(request.getQuery());
        searchWordsNormalForms = (String[]) setRequestLemmas.toArray();
        return null;
    }

    TreeMap<Integer, String> filterPopularLemmas(Set<String> text) {
        for (String lemma : text) {

        }

        return null;
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
}
