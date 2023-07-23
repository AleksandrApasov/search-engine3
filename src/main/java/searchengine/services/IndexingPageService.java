package searchengine.services;

import searchengine.dto.statistics.IndexPageResponse;

import java.io.IOException;
import java.util.HashMap;

public interface IndexingPageService {
    IndexPageResponse lematizate(String url) ;
}
