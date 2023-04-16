package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "page")
@NoArgsConstructor
@Getter
@Setter
@Table(name = "page", indexes = @Index(columnList = "path"))
public class Page {
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    Long id;

   @Column(name = "site_id", nullable = false)
   private Long siteId;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
}
