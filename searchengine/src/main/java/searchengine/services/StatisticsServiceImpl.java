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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            item.setStatus(modelSite.getType().toString());
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setError(modelSite.getLastError()==null ? "" : modelSite.getLastError());
            item.setStatusTime(getTimestamp(modelSite.getStatusTime()));
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

    private long getTimestamp(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyy hh:mm:ss");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        return timestamp.getTime() + 43200000;
    }
}
