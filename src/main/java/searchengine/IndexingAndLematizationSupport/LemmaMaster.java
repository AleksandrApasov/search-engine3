package searchengine.IndexingAndLematizationSupport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Getter
public class LemmaMaster {
    public HashMap<String, Integer> lematizating(String text) {
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
        return lemmas;
    }
}
