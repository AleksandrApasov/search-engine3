package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexPageResponse;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Getter
public class IndexingPageServiceImpl implements IndexingPageService {

    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;

    private SiteRepository siteRepository;

    private final SitesList sitesList;

    @Override
    public IndexPageResponse lematizate(String url) {

        boolean result = true;
        int codeConnect = 200;
        String error = "";
        String text = "";


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
                if (site.getUrl().contains(url)) {
                    page.setSite(site);
                }
            }
        } catch (NullPointerException nullPointerException){
            System.out.println(nullPointerException.fillInStackTrace());
        }




        if (page.getSite() == null) {
            for (searchengine.config.Site site : sitesList.getSites()) {
                if (site.getUrl().contains(url)) {
                    Site site1 = new Site();
                    site1.setName(site.getName());
                    site1.setStatus_time(LocalDateTime.now());
                    site1.setUrl(url);
                    siteRepository.save(site1);
                    page.setSite(site1);
                }
            }
        }

        if (page.getSite() == null){
            result = false;
        } else {
            pageRepository.save(page);
        }



        HashMap<String, Integer> lemmas = new HashMap<>();
        ArrayList<String> tempArray = new ArrayList<>();


        String regex = " ";
        String formattesText = text.replaceAll("[^А-Яа-я]", " ");
        String formattesText2 = formattesText.replaceAll(" +", " ");

        String finalTextFormatted = formattesText2.replaceAll("([А-Я])", "$1").toLowerCase();
        if (finalTextFormatted.indexOf(" ") == 0) {
            finalTextFormatted = finalTextFormatted.replaceFirst(" ", "");
        }
        System.out.println(finalTextFormatted);
        String[] splitText = finalTextFormatted.split(regex);

//        for (String fefe : splitText){
//            System.out.println(fefe);
//        }

        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            for (String word : splitText) {

                List<String> info = luceneMorph.getMorphInfo(word);


                // System.out.println(info);
                if (!((info.toString().contains("МЕЖД")) ||
                        info.toString().contains("ПРЕДЛ") ||
                        info.toString().contains("СОЮЗ"))) {
                    List<String> wordBaseForms = luceneMorph.getNormalForms(word);
                    tempArray.addAll(wordBaseForms);
                }
            }


        } catch (
                Exception ex) {
            System.out.println(ex.fillInStackTrace());
        }

        for (
                String word : tempArray) {
            int i = 1;
            if (lemmas.containsKey(word)) {
                i = lemmas.get(word) + 1;
            }
            lemmas.put(word, i);
        }


        for (
                Map.Entry<String, Integer> map : lemmas.entrySet()) {
            System.out.println(map.getKey() + "  " + map.getValue());
        }


        IndexPageResponse response = new IndexPageResponse();
        if (!result) {
            response.setError("Данная страница находится за пределами сайтов,\n" +
                    "указанных в конфигурационном файле");
        }
        response.setResult(result);
        return response;


    }
}
