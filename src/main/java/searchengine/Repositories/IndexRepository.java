package searchengine.Repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.index;
import searchengine.model.lemma;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<index, Integer> {
    List<index> findByLemma(lemma lemma);
    List<index> findByPage(Page page);
    index findByPageAndLemma(Page page, lemma lemma);
}
