package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Entity(name = "site")
@NoArgsConstructor
@Getter
@Setter
@Table(name = "site")
public class Site {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    private StatusType type;

    @Column(name = "status_time")
    private String statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;
}
