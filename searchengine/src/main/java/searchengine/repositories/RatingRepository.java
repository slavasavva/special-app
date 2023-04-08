package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RatingRepository {

    @Query(value = "SELECT lemma_id from rating WHERE page_id = :pageId", nativeQuery = true)
    List<Long> getLemmasIgByPageId(Long pageId);
    @Modifying
    @Query(value = "DELETE from rating WHERE page_id = :pageId", nativeQuery = true)
    @Transactional
    void deleteByPageId(Long pageId);
}
