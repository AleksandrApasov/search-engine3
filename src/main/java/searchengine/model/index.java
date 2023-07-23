package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "index1")
@Getter
@Setter
public class index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    Page page;

    @ManyToOne(cascade = CascadeType.PERSIST)
    lemma lemma;

    @Column(columnDefinition = "FLOAT NOT NULL")
    float rank1;
}
