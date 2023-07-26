package searchengine.Repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.lemma;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<lemma, Integer> {
    List<lemma> findBySite(Site site);
    List<lemma> findByLemma(String lemma);
    lemma findByLemmaAndSite(String lemma, Site site);
}
