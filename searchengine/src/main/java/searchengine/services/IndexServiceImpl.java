package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.StartIndexingResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SitesList configSites;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    @Override
    public StartIndexingResponse startIndexing() {
        addAll();
        StartIndexingResponse startIndexingResponse = new StartIndexingResponse();
        startIndexingResponse.setResult(true);
        return startIndexingResponse;
    }

    public void addAll() {
        configSites.getSites().forEach(configSite -> {
            Long oldSiteId = getSiteIdByUrl(configSite.getUrl());
            deleteSiteByUrl(configSite.getUrl());
            deletePageBySiteId(oldSiteId);
            Long siteId = addSite(configSite);
            addPage(siteId, "savva", 200, "kamu");
        });
    }

    private Long getSiteIdByUrl(String url) {
        return siteRepository.findByUrl(url).get(0).getId();
    }

    private void deleteSiteByUrl(String url) {
        siteRepository.deleteByUrl(url);
    }

    private void deletePageBySiteId(Long siteId) {
        pageRepository.deleteBySiteId(siteId);
    }

    private Long addSite(searchengine.config.Site configSite) {
        Site site = new Site();
        site.setType(StatusType.INDEXING);
        site.setUrl(configSite.getUrl());
        site.setName(configSite.getName());
        return siteRepository.save(site).getId();
    }

    private void addPage(Long siteId, String path, int code, String content) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        pageRepository.save(page);
    }
}
