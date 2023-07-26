package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.IndexingAndLematizationSupport.IndexingService;
import searchengine.IndexingAndLematizationSupport.LemmaMaster;
import searchengine.Repositories.IndexRepository;
import searchengine.Repositories.LemmaRepository;
import searchengine.Repositories.PageRepository;
import searchengine.Repositories.SiteRepository;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexPageResponse;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.dto.statistics.StopResponse;
import searchengine.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
@Getter
@Transactional
public class RepositoryAndIndexManagerImpl implements RepositoryAndIndexManager {


//    public RepositoryAndIndexManagerImpl(PageRepository pageRepository, SiteRepository siteRepository, SitesList sites) {
//        this.pageRepository = pageRepository;
//        this.siteRepository = siteRepository;
//        this.sites = sites;
//    }


    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private SiteRepository siteRepository;
    private final SitesList sites;
    static String status = "";

    private LemmaMaster lemmaMaster = new LemmaMaster();

    private ForkJoinPool forkJoinPool = new ForkJoinPool();


    @Override
    @Async
    @Transactional
    public void sitesIndexing() {
        // ExecutorService executorService = Executors.newSingleThreadExecutor();
        //executorService.execute(() -> {
          sitesIndexingStart();
       // });
        //tester();

    }


    public void tester() {
        for (int j = 0; j < 10000; j++) {
            for (int i = 0; i < 10000; i++) {
                System.out.println("10101010010000010ПРОВЕРКА100010101001010");
                System.out.println("110101010010000010ПРОВЕРКА100010101001010");
                System.out.println("0110101010010000010ПРОВЕРКА100010101001010");
                System.out.println("00110101010010000010ПРОВЕРКА100010101001010");
                System.out.println("100110101010010000010ПРОВЕРКА100010101001010");
                System.out.println("00110101010010000010ПРОВЕРКА100010101001010");
                System.out.println("0110101010010000010ПРОВЕРКА100010101001010");
                System.out.println("110101010010000010ПРОВЕРКА100010101001010");
            }
        }
    }
    public void sitesIndexingStart() {


        String[] errors = {
                "Индексация уже запущена",
                ""
        };
        IndexingResponse indexingResponse = new IndexingResponse();

        if (status.equals("INDEXING")) {
            indexingResponse.setResult(false);
            indexingResponse.setError(errors[0]);
            //return indexingResponse;
        }
        status = "INDEXING";


        Iterable<Site> siteIterable;
        ArrayList<Page> pages = new ArrayList<>();
        try {
            siteIterable = siteRepository.findAll();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
        }


        for (searchengine.config.Site site : sites.getSites()) {
            searchengine.model.Site site1 = new searchengine.model.Site();
            if (!(siteRepository.findByUrl(site.getUrl()) == null)) {
                site1 = siteRepository.findByUrl(site.getUrl());
            }

            site1.setStatus_time(LocalDateTime.now());
            site1.setUrl(site.getUrl());
            site1.setName(site.getName());
            site1.setStatus(StatusType.INDEXING);
            site1.setLast_error("  ");
            siteRepository.save(site1);
            IndexingService indexingService = new IndexingService(site.getUrl(), 0);
            ArrayList<Page> pagesNotFinal = forkJoinPool.invoke(indexingService);
            site1.setLast_error(indexingService.getErrorStatus() + " - статус первой страницы " +
                    IndexingService.lastError + "последняя ошибка индексации");


            for (Page page : pagesNotFinal) {
                boolean isPagePereIndexed = false;

                if (!(pageRepository.findByPath(page.getPath()) == null)) {
                    Page page1 = pageRepository.findByPath(page.getPath());
                    page.setId(page1.getId());
                    isPagePereIndexed = true;

//                    indexRepository.findByPage(page1).forEach(s -> {
//                        lemma lemmaForDeleting = s.getLemma();
//                        indexRepository.delete(s);
//                        lemmaRepository.delete(lemmaForDeleting);
//
//
//                    });
//                    pageRepository.delete(page1);

                }
                page.setSite(site1);
               // synchronized (this) {
                    pageRepository.save(page);
                //}
                addLemmaAndIndex(page, site1,isPagePereIndexed);





            }
            site1.setStatus(StatusType.INDEXED);
            //TODO: synchronized (siteRepository) {
                siteRepository.save(site1);

        }

        indexingResponse.setResult(true);
        indexingResponse.setError(errors[1]);
        status = "NOT INDEXING";
        //return indexingResponse;
    }

    public boolean inWork() {
        if (status.equals("INDEXING")) {
            return true;
        }
        return false;
    }

    @Override
    public StopResponse stopIndexing() {
        StopResponse stopResponse = new StopResponse();
        if (!forkJoinPool.isTerminating()) {
            stopResponse.setError("Индексация не запущена");
            stopResponse.setResult(false);
        }
        forkJoinPool.shutdown();
        status = "FAILED";
        stopResponse.setResult(true);
        stopResponse.setError("");
        return stopResponse;
    }

    @Override
    public IndexPageResponse onePageIndexing(String url) {

        boolean result = true;
        int codeConnect = 200;
        String error = "";
        String text = "";

        try {
            Iterable<index> indexes = indexRepository.findAll();
            for (index index1 : indexes) {
                System.out.println(index1.getPage().getPath() + "<--из инд..  ..тек урл-->" + url);
                if (index1.getPage().getPath().equals(url)) {
                    indexRepository.delete(index1);
                    pageRepository.delete(index1.getPage());
                    lemmaRepository.delete(index1.getLemma());
                }
            }
        } catch (NullPointerException nullPointerException) {
            System.out.println(nullPointerException.fillInStackTrace());
        }


        try {
            text = Jsoup.connect(url).get().toString();

        } catch (HttpStatusException httpEx) {
            codeConnect = httpEx.getStatusCode();
        } catch (Exception ex) {
            System.out.println(ex.fillInStackTrace());
            result = false;
        }

        Page page = new Page();
        page.setPath(url);
        page.setContent(text);
        page.setCode(codeConnect);

        try {
            Iterable<searchengine.model.Site> sites = siteRepository.findAll();
            for (Site site : sites) {
                System.out.println(site + "  " + url);
                if (url.contains(site.getUrl())) {
                    page.setSite(site);
                }
            }
        } catch (NullPointerException nullPointerException) {
            System.out.println(nullPointerException.fillInStackTrace());
        }

        Site site1 = new Site();
        if (page.getSite() == null) {
            for (searchengine.config.Site site : sites.getSites()) {
                System.out.println(site.getUrl() + "  " + url);
                if (url.contains(site.getUrl())) {

                    site1.setName(site.getName());
                    site1.setStatus_time(LocalDateTime.now());
                    site1.setUrl(url);
                    siteRepository.save(site1);
                    page.setSite(site1);
                }
            }
        }


        if (page.getSite() == null) {
            result = false;
        } else {
            pageRepository.save(page);
        }


        HashMap<String, Integer> lemmas = lemmaMaster.lematizating(text);


        addLemmaAndIndex(page, site1,false);


        IndexPageResponse response = new IndexPageResponse();
        if (!result) {
            response.setError("Данная страница находится за пределами сайтов,\n" +
                    "указанных в конфигурационном файле");
        }
        response.setResult(result);
        return response;


    }



    public void addLemmaAndIndex(Page page, Site site, boolean isPageOnPereIndexing) {
        HashMap<String, Integer> lemmasFromPage = new HashMap<>();
        try {
            lemmasFromPage = lemmaMaster.lematizating(page.getContent());

            for (String key : lemmasFromPage.keySet()) {
                if (!(lemmaRepository.findByLemmaAndSite(key, page.getSite()) == null)) {

                    System.out.println("МЫ НАШЛИ УЖЕ СОЗДАННУЮ ЛЕММУ" + lemmaRepository.findByLemmaAndSite(key, page.getSite()));
//            for (lemma lemFromRep : lemmasListFromRep) {
//                System.out.println(lemFromRep.getLemma() + " --- из бд;  ");
//

                    lemma lemFromRep = lemmaRepository.findByLemmaAndSite(key, page.getSite());
//                if (lemmasFromPage.containsKey(lemFromRep.getLemma())) {
                    int frequency = lemFromRep.getFrequency();
                    if (!isPageOnPereIndexing) {
                        frequency++;
                    }
                    lemFromRep.setFrequency(frequency);
                    lemmaRepository.save(lemFromRep);


                    index index1 = new index();
                    if (!(indexRepository.findByPageAndLemma(page,lemFromRep) == null)){
                        index1.setId(indexRepository.findByPageAndLemma(page,lemFromRep).getId());
                    }
                    index1.setPage(page);
                    index1.setLemma(lemFromRep);
                    index1.setRank1(lemmasFromPage.get(lemFromRep.getLemma()));
                    indexRepository.save(index1);

                } else {
                    lemma lemma1 = new lemma();
                    lemma1.setFrequency(1);
                    lemma1.setSite(page.getSite());
                    lemma1.setLemma(key);
                    //TODO: synchronized (lemmaRepository) {
                        lemmaRepository.save(lemma1);
                   // }

                    index index1 = new index();
                    index1.setPage(page);
                    index1.setLemma(lemma1);
                    index1.setRank1(lemmasFromPage.get(key));
                    //TODO: synchronized (indexRepository) {
                        indexRepository.save(index1);
                   // }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}
