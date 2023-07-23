package searchengine.services;

import searchengine.dto.statistics.IndexPageResponse;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.dto.statistics.StopResponse;

public interface RepositoryAndIndexManager {

    void sitesIndexing();
    public IndexPageResponse onePageIndexing(String url);
    public StopResponse stopIndexing();
    public boolean inWork();
}
