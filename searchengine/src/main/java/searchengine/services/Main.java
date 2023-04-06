package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    SiteRepository siteRepository;
    LemmaFinder lemmaFinder;
    public static void main(String[] args) {
        
        LuceneMorphology luceneMorph =
                null;
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> wordBaseForms =
                luceneMorph.getNormalForms("математике");
//        wordBaseForms.forEach(System.out::println);
        LemmaFinder lemmaFinder = new LemmaFinder();
        SiteRepository siteRepository;
        
        
        HashMap<String, Integer> lemma =
                (HashMap<String, Integer>) lemmaFinder.collectLemmas("розы позы");
        for (int lem : lemma.values()){
            System.out.println(lem);
        }
    }
}
