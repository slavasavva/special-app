package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.WordEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    @Modifying
    @Query(value = "delete from page where site_id = :siteId", nativeQuery = true)
    @Transactional
    void deleteBySiteId(Long siteId);
}
