package searchengine.services.search;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;

public class StringBuilder {
    public static void main(String[] args) throws IOException {
        LuceneMorphology luceneMorph =
                new RussianLuceneMorphology();
        List<String> wordBaseForms =
                luceneMorph.getNormalForms("дороги");
        wordBaseForms.forEach(System.out::println);
    }
    public String getSnippet(String text, List<String> searchQuery){







        return null;
    }
}
