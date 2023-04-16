package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity(name = "lemma")
@NoArgsConstructor
@Getter
@Setter
@Table(name = "lemma")
public class Lemma {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    Long id;

    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private int frequency;
}
