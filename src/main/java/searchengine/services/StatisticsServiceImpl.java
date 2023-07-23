package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
@Getter
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;
    ArrayList<searchengine.model.Site> sites2 = new ArrayList<>();

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private SiteRepository siteRepository;




    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        Iterable<searchengine.model.Site> siteList = siteRepository.findAll();
        TotalStatistics total = new TotalStatistics();
        total.setSites((int) siteRepository.count());
        total.setIndexing(true);
        Iterable<lemma> lemmasList = lemmaRepository.findAll();
        Iterable<Page> pageList = pageRepository.findAll();

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for(searchengine.model.Site site : siteList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = pageRepository.findBySite(site).size();
            int lemmas = lemmaRepository.findBySite(site).size();
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(site.getStatus().toString());
            if (site.getLast_error().charAt(0) == '4'){
                item.setError(errors[0]);
            }
            if (site.getLast_error().charAt(0) == '5'){
                item.setError(errors[1]);
            } else {
                item.setError(errors[2]);
            }
            item.setStatusTime(site.getStatus_time());
            total.setPages((int) pageRepository.count());
            total.setLemmas((int) lemmaRepository.count());
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }


}
