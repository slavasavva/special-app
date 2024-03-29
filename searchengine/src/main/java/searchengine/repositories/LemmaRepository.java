package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.FilteredLemma;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    @Query(value = "SELECT * from lemma where lemma = :lemma and site_id = :siteId", nativeQuery = true)
    Lemma findByLemmaAndSiteId(String lemma, Long siteId);

    @Query(value = "SELECT count(id) from lemma where site_id = :siteId", nativeQuery = true)
    int getCountLemmasBySiteId(Long siteId);

    @Modifying
    @Query(value = "UPDATE lemma SET frequency = (frequency + 1)  where lemma = :lemma and site_id = :siteId", nativeQuery = true)
    @Transactional
    void increaseFrequencyByLemmaAndSiteId(String lemma, Long siteId);

    @Modifying
    @Query(value = "delete from lemma where site_id = :siteId", nativeQuery = true)
    @Transactional
    void deleteBySiteId(Long siteId);

    @Query(
            value = "select " +
                    "distinct l.lemma as lemma, " +
                    "sum(l.frequency) over (partition by l.lemma) as fr " +
                    "from lemma l " +
                    "join rating r on l.id = r.lemma_id " +
                    "where l.site_id in :siteIds " +
                    "and l.lemma in :lemmas " +
                    "group by l.lemma, l.frequency, l.id " +
                    "having count(r.page_id) < (select cast(count(p.id) as double precision) * :threshold from page p) " +
                    "order by fr asc",
            nativeQuery = true)
    List<FilteredLemma> filterPopularLemmas(
            List<Long> siteIds,
            List<String> lemmas,
            double threshold);
}
