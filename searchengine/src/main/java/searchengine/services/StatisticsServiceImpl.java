package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.IndexingSettings;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final IndexingSettings indexingSettings;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics totalStatistics = new TotalStatistics();
        totalStatistics.setSites(indexingSettings.getSites().size());
        totalStatistics.setIndexing(true);

        List<DetailedStatisticsItem> detailedStatisticsItems = new ArrayList<>();
        List<searchengine.config.Site> configSites = indexingSettings.getSites();
        for (searchengine.config.Site configSite : configSites) {
            Site site = siteRepository.findSiteByUrl(configSite.getUrl());
            if (site == null) {
                continue;
            }
            int pages = pageRepository.countIndexedPage(site.getId());
            int lemmas = lemmaRepository.getCountLemmasBySiteId(site.getId());

            DetailedStatisticsItem detailedStatisticsItem = new DetailedStatisticsItem();
            detailedStatisticsItem.setUrl(configSite.getUrl());
            detailedStatisticsItem.setName(configSite.getName());
            detailedStatisticsItem.setStatus(site.getType().toString());
            detailedStatisticsItem.setPages(pages);
            detailedStatisticsItem.setLemmas(lemmas);
            detailedStatisticsItem.setError(site.getLastError() == null ? "" : site.getLastError());
            detailedStatisticsItem.setStatusTime(getTimestamp(site.getStatusTime()));
            detailedStatisticsItems.add(detailedStatisticsItem);

            totalStatistics.setPages(totalStatistics.getPages() + pages);
            totalStatistics.setLemmas(totalStatistics.getLemmas() + lemmas);
        }

        StatisticsData statisticsData = new StatisticsData();
        statisticsData.setTotal(totalStatistics);
        statisticsData.setDetailed(detailedStatisticsItems);
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        statisticsResponse.setStatistics(statisticsData);
        statisticsResponse.setResult(true);
        return statisticsResponse;
    }

    private long getTimestamp(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyy hh:mm:ss");
        Date parsedDate;
        try {
            parsedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        return timestamp.getTime();
    }
}
