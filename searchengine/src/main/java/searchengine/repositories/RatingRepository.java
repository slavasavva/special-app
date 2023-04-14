package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Rating;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Query(value = "SELECT lemma_id from rating WHERE page_id = :pageId", nativeQuery = true)
    List<Long> getLemmasIdByPageId(Long pageId);

    @Modifying
    @Query(value = "DELETE from rating WHERE page_id = :pageId", nativeQuery = true)
    @Transactional
    void deleteByPageId(Long pageId);

    @Modifying
    @Query(value = "DELETE from rating r" +
            "join page p on r.page_id = p.id " +
            "join lemma l on l.id = r.lemma_id" +
            "join site s on s.id = p.site_id  + WHERE p.site_id = :siteId" +
            "and p.id = r.id", nativeQuery = true)
    @Transactional
    void deleteBySiteId(Long siteId);


    @Query(
            value = "select r.page_id " +
                    "from rating r " +
                    "join lemma l on l.id = r.lemma_id " +
                    "where l.site_id in :siteIds " +
                    "and l.lemma = :lemma " +
                    "and r.page_id in (:pageIds)",
            nativeQuery = true
    )
    List<Long> findPageIdsBySiteInAndLemmaAndPageIdsIn(
            List<Long> siteIds,
            String lemma,
            List<Long> pageIds);
}
