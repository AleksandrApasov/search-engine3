package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "lemma")
@Getter
@Setter
public class lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT NOT NULL")
    int id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    Site site;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    String lemma;

    @Column(columnDefinition = "INT NOT NULL")
    int frequency;
}
