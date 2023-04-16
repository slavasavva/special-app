package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;


import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    @Query(value = "SELECT * from site where url = :url", nativeQuery = true)
    List<Site> findByUrl(String url);

    @Query(value = "SELECT * from site where url = :url", nativeQuery = true)
    Site findSiteByUrl(String url);

    @Query(value = "SELECT id from site where url = :url", nativeQuery = true)
    Long getSiteIdByUrl(String url);

    @Modifying
    @Query(value = "delete from site where url = :url", nativeQuery = true)
    @Transactional
    void deleteByUrl(String url);

    @Modifying
    @Query(value = "update site set type = :type where url = :url", nativeQuery = true)
    @Transactional
    void setType(String url, String type);

    @Modifying
    @Query(value = "update site set last_error = :error where url = :url", nativeQuery = true)
    @Transactional
    void setLastError(String url, String error);

    @Modifying
    @Query(value = "update site set status_time = :statusTime where url = :url", nativeQuery = true)
    @Transactional
    void statusTime(String url, String statusTime);
}
