package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.PageDTO;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    @Query(value = "SELECT * from page WHERE site_id = :siteId and path = :path", nativeQuery = true)
    List<Page> findBySiteIdAndPath(Long siteId, String path);

    @Query(value = "SELECT * from page WHERE page_id = :pageId", nativeQuery = true)
    Page findPageById(Long pageId);

    @Modifying
    @Query(value = "DELETE from page WHERE site_id = :siteId", nativeQuery = true)
    @Transactional
    void deleteBySiteId(Long siteId);

    @Modifying
    @Query(value = "DELETE from page WHERE id = :Id", nativeQuery = true)
    @Transactional
    void deleteById(Long Id);

    @Query(value = "SELECT count(id) FROM search_engine.page where site_id = :siteId", nativeQuery = true)
    int countIndexedPage(Long siteId);

    @Query(value = "SELECT count(id) FROM search_engine.page where site_id = :siteId and code = 200", nativeQuery = true)
    int countSuccessfulIndexedPage(Long siteId);

    @Query(value = "SELECT id FROM search_engine.page where path = :path", nativeQuery = true)
    Long getPageIdByPath(String path);

    @Query(value = "select p.id from page p where p.site_id in :siteIds", nativeQuery = true)
    List<Long> getAllIdsBySiteId(List<Long> siteIds);

    @Query(
            value = "select distinct " +
                    "s.url as siteUrl, " +
                    "s.name as siteName, " +
                    "p.path as path, " +
                    "p.content as content, " +
                    "sum(r.rating) over (partition by p.path) as relevance " +
                    "from page p " +
                    "join rating r on p.id = r.page_id " +
                    "join lemma l on l.id = r.lemma_id " +
                    "join site s on s.id = p.site_id " +
                    "where l.lemma in :lemmas " +
                    "and p.id in :pageIds " +
                    "order by relevance desc " +
                    "limit :limit " +
                    "offset :offset",
            nativeQuery = true
    )
    List<PageDTO> getLimitedSortedPagesByLemmasAndPageIds(
            List<String> lemmas,
            List<Long> pageIds,
            int limit,
            int offset);
}
