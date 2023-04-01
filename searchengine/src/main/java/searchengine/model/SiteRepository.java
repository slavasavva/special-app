package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    /**
     * @param wordPart часть слова
     * @param limit макс количество результатов
     * @return список подходящих слов
     *
     * <p>Для создания SQL запроса, необходимо указать nativeQuery = true</p>
     * <p>каждый параметр в SQL запросе можно вставить, используя запись :ИМЯ_ПЕРЕМEННОЙ
     * перед именем двоеточие, так hibernate поймет, что надо заменить на значение переменной</p>
     */
    @Query(value = "SELECT * from words where word LIKE %:wordPart% LIMIT :limit", nativeQuery = true)
    List<WordEntity> findAllContains(String wordPart, int limit);
}