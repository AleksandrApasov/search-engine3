package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.*;
import searchengine.model.Page;
import searchengine.model.PageRepository;
import searchengine.model.Site;
import searchengine.model.SiteRepository;
import searchengine.services.*;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class ApiController {



    private final StatisticsService statisticsService;
    private final IndexingPageService indexingPageService;
    private final RepositoryAndIndexManager repositoryAndIndexManager;
    private final SearchService searchService;




    public ApiController(StatisticsService statisticsService, IndexingPageService indexingPageService,
                         RepositoryAndIndexManager repositoryAndIndexManager, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingPageService = indexingPageService;
        this.repositoryAndIndexManager = repositoryAndIndexManager;
        this.searchService = searchService;


    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        boolean inWork = repositoryAndIndexManager.inWork();
        IndexingResponse indexingResponse = new IndexingResponse();
        if (inWork) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация уже запущена.");
        } else {
            repositoryAndIndexManager.sitesIndexing();
            indexingResponse.setResult(true);
            indexingResponse.setError("");
        }
        return ResponseEntity.ok(indexingResponse);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexPageResponse> indexPage(String url) {
            return ResponseEntity.ok(repositoryAndIndexManager.onePageIndexing(url));


        }
    @GetMapping("/stopIndexing")
    public ResponseEntity<StopResponse> stopIndexing() {
        return ResponseEntity.ok(repositoryAndIndexManager.stopIndexing());
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> statistics(String query, String site,int offset, int limit) {
        return ResponseEntity.ok(searchService.search(query,site,offset,limit));
    }

}
