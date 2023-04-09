package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;


import java.util.List;
@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    @Query(value = "SELECT * from words where word LIKE %:wordPart% LIMIT :limit", nativeQuery = true)
    List<Lemma> findAllContains(String wordPart, int limit);

    @Query(value = "SELECT count(id) from lemma where lemma = :lemma", nativeQuery = true)
    int checkLemmaContaint(String lemma);

    @Modifying
    @Query(value = "delete from lemma where id = :Id", nativeQuery = true)
    @Transactional
    void deleteById(Long Id);

}
