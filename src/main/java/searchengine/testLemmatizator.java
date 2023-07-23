package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testLemmatizator {
    public static void main(String[] args) throws IOException {



        String text = "Алексей, добрый день! Мой гитхаб — https://github.com/, а также ссылка на мой персональный сайт — https://www.skillbox.ru/ Если возникнут вопросы, пишите мне напрямую. Я всегда доступен";
        String regex = "[^.]{10}https://[^,\\s]+[^.]{20}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }



//        String text = Jsoup.connect("http://www.lenta.ru/").get().toString();
//
//
//
//        for (Map.Entry<String,Integer> map : lematizate(text).entrySet()){
//            System.out.println(map.getKey() + "  " + map.getValue());
//        }


    }

    static public HashMap<String, Integer> lematizate(String text){

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


        } catch (Exception ex) {
            System.out.println(ex.fillInStackTrace());
        }

        for (String word : tempArray){
            int i = 1;
            if (lemmas.containsKey(word)){
                i = lemmas.get(word) + 1;
            }
            lemmas.put(word,i);
        }



        return lemmas;



    }

}
