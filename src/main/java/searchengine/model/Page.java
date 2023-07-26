package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
//
@Entity
@Table(name = "page", indexes = {@Index(name = "path_index", columnList = "path, site_id", unique = true)})
@Getter
@Setter
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    Site site;
    String path;
    int code;
    @Column(columnDefinition = "MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    String content;
}
