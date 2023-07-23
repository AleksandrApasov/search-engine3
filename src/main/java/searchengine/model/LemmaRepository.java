package searchengine.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<lemma, Integer> {
    List<lemma> findBySite(Site site);
    List<lemma> findByLemma(String lemma);
    lemma findByLemmaAndSite(String lemma, Site site);
}
