package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.WordEntity;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    @Query(value = "select * from site where url = :url", nativeQuery = true)
    List<Site> findByUrl(String url);

    @Modifying
    @Query(value = "delete from site where url = :url", nativeQuery = true)
    @Transactional
    void deleteByUrl(String url);
}
