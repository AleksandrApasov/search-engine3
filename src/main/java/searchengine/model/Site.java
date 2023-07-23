package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "site")
@Getter
@Setter
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    StatusType status;

    LocalDateTime status_time;
    @Column(columnDefinition = "VARCHAR(255)")
    String last_error;
    @Column(columnDefinition = "TEXT")
    String url;
    @Column(columnDefinition = "MEDIUMTEXT")
    String name;
}
