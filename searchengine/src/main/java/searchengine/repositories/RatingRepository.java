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
    List<Long> getLemmasIgByPageId(Long pageId);
    @Modifying
    @Query(value = "DELETE from rating WHERE page_id = :pageId", nativeQuery = true)
    @Transactional
    void deleteByPageId(Long pageId);
}
