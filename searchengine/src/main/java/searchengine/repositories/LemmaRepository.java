package searchengine.repositories;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import java.util.List;

public interface LemmaRepository {
    @Query(value = "SELECT * from words where word LIKE %:wordPart% LIMIT :limit", nativeQuery = true)
    List<Lemma> findAllContains(String wordPart, int limit);

    @Modifying
    @Query(value = "delete from lemma where id = :Id", nativeQuery = true)
    @Transactional
    void deleteById(Long Id);

}
