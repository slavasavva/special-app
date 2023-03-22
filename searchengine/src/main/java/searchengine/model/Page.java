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
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(columnDefinition = "site_id", nullable = false)
    private String siteId;

    @Column(nullable = false)

    private String path;

    @Column(nullable = false)
    private String code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
}
