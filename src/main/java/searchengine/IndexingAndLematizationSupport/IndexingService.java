package searchengine.IndexingAndLematizationSupport;

import lombok.Getter;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;


@Getter
public class IndexingService extends RecursiveTask<ArrayList<Page>> {
        static ArrayList<String> urlFinal = new ArrayList<>();

        int i;

        Site site;

        int errorStatus = 200;
        public static String lastError;
        String firstUrl;
        public IndexingService(String firstUrl, int currantNum){
            this.firstUrl = firstUrl;
            i = currantNum;

            //this.site = site;
        }
        @Override
        protected ArrayList<Page> compute() {
            List<IndexingService> taskList = new ArrayList<>();
            ArrayList<Page> pages = new ArrayList<>();
//            if (i > 5) {
//                return pages;
//            }
            Document doc = null;
            try {
                doc = Jsoup.connect(firstUrl)
                        .userAgent("Mozilla/5.0")
                        .referrer("http://www.google.com")
                        .get();

                //  doc = Jsoup.connect(firstUrl).get();
            } catch (HttpStatusException httpEx) {
                errorStatus = httpEx.getStatusCode();
                lastError = httpEx.fillInStackTrace().toString();
            } catch (IOException e) {
                System.out.println(e.fillInStackTrace());
                lastError = e.fillInStackTrace().toString();
            }
            if (!(doc == null)) {
                Elements links = doc.select("a");
                for (Element link : links) {
                    String url = link.absUrl("href");
                    if ((!urlFinal.contains(url))
                            && (url.contains(firstUrl))
                            && !(url.substring(url.length() - 1).equals("#")) && (i < 3)) {

                        Page pageTemp = new Page();
                        pageTemp.setPath(link.absUrl("href"));
                        pageTemp.setContent(doc.toString());
                        pageTemp.setCode(errorStatus);
                        Site newSite = new Site();
                        newSite.setUrl(link.absUrl("href"));
                        //pageTemp.setSite(newSite);
                        pages.add(pageTemp);

                        urlFinal.add(link.absUrl("href"));
                        IndexingService task = new IndexingService(link.absUrl("href"), i);
                        task.fork();
                        taskList.add(task);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(i++ + "   " + link.absUrl("href"));
                    }

                }
                for (IndexingService task : taskList) {

                    pages.addAll(task.join());

                }
            }
            return pages;
        }
    }

