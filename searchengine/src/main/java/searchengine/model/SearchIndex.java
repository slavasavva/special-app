package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Entity(name = "search_index")
@NoArgsConstructor
@Getter
@Setter
@Table(name = "search_index")
public class SearchIndex {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    Long id;

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(name = "lemma_id", nullable = false)
    private Long lemmaId;


    @Column(nullable = false)
    private Float rank;
}
