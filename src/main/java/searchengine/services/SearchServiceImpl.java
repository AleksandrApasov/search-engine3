package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.SearchData;
import searchengine.dto.statistics.SearchResponse;
import searchengine.model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Getter
public class SearchServiceImpl implements SearchService {


    private LemmaMaster lemmaMaster = new LemmaMaster();
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private SiteRepository siteRepository;
    long pageCount;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {

        SearchResponse searchResponse = new SearchResponse();
        if (query.equals("")) {
            searchResponse.setResult(false);
            searchResponse.setError("Задан пустой поисковый запрос");
            return searchResponse;
        }

        HashMap<String, Integer> lemmas = lemmaMaster.lematizating(query);
        HashMap<String, Integer> lemmasNew = new HashMap<String, Integer>();
        pageCount = pageRepository.count();

        for (Map.Entry<String, Integer> map : lemmas.entrySet()) {
            System.out.println("Без сортировки - " + map.getKey() + "  " + map.getValue());
            List<lemma> lemmaList;
            int allFrequency = 0;
            lemmaList = lemmaRepository.findByLemma(map.getKey());

            for (lemma lemma1 : lemmaList) {
                allFrequency += lemma1.getFrequency();
            }
            if ((!(allFrequency == 0)) && !(isLemmaTooOften(lemmaList))) {

                System.out.println("фрикунси  " + allFrequency);
                lemmasNew.put(map.getKey(), allFrequency);
                // ||
            }

        }


        Map<String, Integer> lemmasSortedByFrequency = sortByValue(lemmasNew);
        for (Map.Entry<String, Integer> mapSearchingLemmas : lemmasSortedByFrequency.entrySet()) {
            System.out.println(mapSearchingLemmas.getKey() + " ---- " + mapSearchingLemmas.getValue());

        }

        ArrayList<Page> pagesRelevantToSearch = new ArrayList<>();

        String rarestLem = "";
        //Map.Entry<String, Integer> map = lemmasSortedByFrequency.entrySet().iterator().next();
        if (!lemmasSortedByFrequency.isEmpty()) {
            rarestLem = lemmasSortedByFrequency.keySet().stream().findFirst().get();
        } else {
            searchResponse.setError("Поисковый запрос встречается слишком часто, добавьте уточнение");
            searchResponse.setResult(false);
        }
        List<lemma> listLem = lemmaRepository.findByLemma(rarestLem);
        for (lemma lemma1 : listLem) {
            List<index> listIndex = indexRepository.findByLemma(lemma1);
            for (index index1 : listIndex) {
                pagesRelevantToSearch.add(index1.getPage());

            }

        }

        for (Map.Entry<String, Integer> mapSearchingLemmas : lemmasSortedByFrequency.entrySet()) {
            System.out.println(mapSearchingLemmas.getKey() + " ---- " + mapSearchingLemmas.getValue());
            pagesRelevantToSearch = searchExactlyPages(pagesRelevantToSearch, mapSearchingLemmas.getKey());
        }
        pagesRelevantToSearch.forEach(s -> System.out.println("итоговые найденные страницы " + s.getPath()));

        HashMap<Page, Double> pagesWithRelevant = new HashMap<>();
        ArrayList<String> searchLemmasAsWords = new ArrayList<>(lemmasSortedByFrequency.keySet());

        double maxAbsRelevant = 0;
        for (Page page : pagesRelevantToSearch) {
            double absRelevant = 0;
            for (String lemma : searchLemmasAsWords) {
                Site searchingSite = getSiteFromPageOrQuery(page, site);
                if (!(lemmaRepository.findByLemmaAndSite(lemma, searchingSite) == null)) {
                    searchengine.model.lemma lemma1 = lemmaRepository.findByLemmaAndSite(lemma, searchingSite);
                    absRelevant += indexRepository.findByPageAndLemma(page, lemma1).getRank1();
                    System.out.println("ПОЛУЧИЛОЬ!!!! ---" + indexRepository.findByPageAndLemma(page, lemma1).getLemma().getLemma() + " " +
                            indexRepository.findByPageAndLemma(page, lemma1).getRank1());
                }

            }
            if (absRelevant > maxAbsRelevant) {
                maxAbsRelevant = absRelevant;
            }
            if (!(absRelevant == 0)) {
                pagesWithRelevant.put(page, absRelevant);
            }
        }


        for (Map.Entry<Page, Double> map : pagesWithRelevant.entrySet()) {
            map.setValue(map.getValue() / maxAbsRelevant);
        }
        Map<Page, Double> sortedRelevantPages = sortByValueReversed(pagesWithRelevant);

        int localLimit = 100000;
        int pagesInResultList = 0;
        if (!(limit == 0)){
            localLimit = limit;
        }

        int countPagesInResultChecked = 0;


        List<SearchData> searchDataList = new ArrayList<>();
        for (Page page : sortedRelevantPages.keySet()) {
            SearchData searchData = new SearchData();
            searchData.setRelevance(sortedRelevantPages.get(page));
            searchData.setSite(page.getSite().getUrl());
            searchData.setUri(page.getPath().replaceAll(page.getSite().getUrl(), ""));
            searchData.setSiteName(page.getSite().getName());


            Document document = Jsoup.parse(page.getContent());

            String title = document.title();
            searchData.setTitle(title);

            String content = page.getContent();

            String formattesText = content.replaceAll("[^А-Яа-я.]", " ");
            String formattesText2 = formattesText.replaceAll(" +", " ");
            String[] querys = query.replaceAll("[^А-Яа-я]", " ").
                    replaceAll(" +", " ").split(" ");
            String[] snippets = new String[lemmasSortedByFrequency.size()];

            String resultSnippet = getSnippet(querys, formattesText2);
            searchData.setSnippet(resultSnippet);


            if ((!resultSnippet.equals("")) && (pagesInResultList < localLimit) && (++countPagesInResultChecked > offset)) {
                searchDataList.add(searchData);
                pagesInResultList++;
            }
            System.out.println("Страница: " + page.getPath() + " Релевантность: " + sortedRelevantPages.get(page));
        }
        searchResponse.setData(searchDataList);
        searchResponse.setResult(true);
        searchResponse.setCount(sortedRelevantPages.size());
        searchResponse.setError("");


        return searchResponse;
    }


    public Site getSiteFromPageOrQuery(Page page, String site){


        if(!(siteRepository.findByUrl(site) == null)){
            return siteRepository.findByUrl(site);
        }
        return page.getSite();
    }


    public String getSnippet(String[] querys, String formattesText2) {
        String resultSnippet = "";
        String localText = formattesText2;
        HashMap<String, Integer> snippetsWithLocalRelevant = new HashMap<>();
        for (String word : querys) {

            String regex = "[А-Яа-я .,]{20}" + word + "[А-Яа-я .,]{20}";

            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher matcher = pattern.matcher(localText);
            while (matcher.find()) {
                String temporarrySnippet = matcher.group();
                snippetsWithLocalRelevant.put(temporarrySnippet, 1);
                localText = localText.replaceAll(temporarrySnippet, "");

            }

            HashMap<String, Integer> tempHash = new HashMap<>();
            for (Map.Entry<String, Integer> map : snippetsWithLocalRelevant.entrySet()) {
                if (map.getKey().contains(word)) {
                    String keyString = map.getKey().replaceAll(word, "<b>" + word + "</b>");
                    tempHash.put(keyString, map.getValue() + 1);

                }
                else {
                    tempHash.put(map.getKey(), map.getValue());
                }
            }
            snippetsWithLocalRelevant.clear();
            snippetsWithLocalRelevant.putAll(tempHash);


        }

        Map<String, Integer> sortedMap = sortByValueReversed(snippetsWithLocalRelevant);
        int limitOfSnippetsParts = 0;
        for (String key : sortedMap.keySet()) {
            if (limitOfSnippetsParts < 5) {
                resultSnippet += " ..." + key + "... ";
                limitOfSnippetsParts++;
            }
        }
        return resultSnippet;
    }

    public ArrayList<Page> searchExactlyPages(ArrayList<Page> pagesRelevantToSearch, String searchingLemma) {
        ArrayList<Page> pagesRelevantToSearch2 = new ArrayList<>();
        for (Page page : pagesRelevantToSearch) {

            List<index> indexList2 = indexRepository.findByPage(page);
            for (index index : indexList2) {
                //System.out.println(index.getLemma().getLemma() + " эквивалентно? " + searchingLemma);
                if (index.getLemma().getLemma().equals(searchingLemma)) {
                    pagesRelevantToSearch2.add(page);
                    System.out.println("\n" + index.getLemma().getLemma() + " соответсвует  " + searchingLemma + "\n");
                }
            }
        }
        return pagesRelevantToSearch2;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueReversed(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();

        for (int i = list.size() - 1; i > 0; i--) {
            result.put(list.get(i).getKey(), list.get(i).getValue());
        }

        return result;
    }

    public boolean isLemmaTooOften(List<lemma> list) {
        for (lemma lemma1 : list) {
            long containsCount = indexRepository.findByLemma(lemma1).size();
            if (((containsCount * 100) / pageCount > 100)) {
                return true;
            }
        }
        return false;
    }
}
