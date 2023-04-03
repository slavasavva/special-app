package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    @Query(value = "select * from page where site_id = :siteId and path = :path", nativeQuery = true)
    List<Site> findBySiteIdAndPath(Long siteId, String path);

    @Modifying
    @Query(value = "delete from page where site_id = :siteId", nativeQuery = true)
    @Transactional
    void deleteBySiteId(Long siteId);
}
