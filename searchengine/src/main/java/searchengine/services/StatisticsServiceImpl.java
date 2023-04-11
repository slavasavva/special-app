package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.IndexingSettings;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final LemmaRepository lemmaRepository;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final Random random = new Random();
    private final IndexingSettings sites;

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = {"INDEXED", "FAILED", "INDEXING"};
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (int i = 0; i < sitesList.size(); i++) {
            searchengine.config.Site site = sitesList.get(i);
            searchengine.model.Site modelSite =
                    siteRepository.findSiteByUrl(site.getUrl());
            int pages = pageRepository.countIndexedPage(modelSite.getId());
            int lemmas = lemmaRepository.getCountLemmasBySiteId(modelSite.getId());

            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setUrl(site.getUrl());
            item.setName(site.getName());
            item.setStatus(statuses[i % 3]);
            item.setStatus(modelSite.getType().toString());
//            int pages = random.nextInt(1_000);
//           int lemmas = pages * random.nextInt(1_000);
            item.setPages(pages);
            item.setLemmas(lemmas);
//            item.setError(errors[i % 3]);
            item.setError(modelSite.getLastError());
            item.setStatusTime(System.currentTimeMillis() -
                    (random.nextInt(10_000)));
//            item.setStatusTime(Long.parseLong(formatter.format(modelSite.getStatusTime())));
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private SimpleDateFormat formatter = new SimpleDateFormat("dd.M.yyyy HH:mm:ss");
}
