package searchengine.repositories;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.Lemma;
import java.util.List;

public interface LemmaRepository {
    @Query(value = "SELECT * from words where word LIKE %:wordPart% LIMIT :limit", nativeQuery = true)
    List<Lemma> findAllContains(String wordPart, int limit);
}
