package searchengine.services.search;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class StringBuilder {
    private LuceneMorphology russianLuceneMorph;
    private LuceneMorphology englishLuceneMorph;

    public String getSnippet(String text, List<String> searchQuery){
        List<String> queryLocalCopy = new ArrayList<>(searchQuery);
        List<String> words = List.of(text.split("\\b"));
        ListIterator<String> iterator;
        List<Integer> foundWordsIndexes = new ArrayList<>();

        for (String word : words) {
            if(queryLocalCopy.isEmpty()) {
                break;
            }
            iterator = queryLocalCopy.listIterator();
            while(iterator.hasNext()) {
                List<String> wordNormalForm = new ArrayList<>(getNormalFormOfAWord(word.toLowerCase(Locale.ROOT)));
                wordNormalForm.retainAll(getNormalFormOfAWord(iterator.next()));
                if(wordNormalForm.isEmpty()) {
                    continue;
                }
                foundWordsIndexes.add(words.indexOf(word));
                iterator.remove();
            }
        }

        return constructSnippetWithHighlight(foundWordsIndexes, new ArrayList<>(words));
    }

    public static String constructSnippetWithHighlight(List<Integer> foundWordsIndexes, List<String> words) {
        List<String> snippetCollector = new ArrayList<>();
        int beginning, end, before, after, index, prevIndex;
        before = 12;
        after = 6;

        foundWordsIndexes.sort(Integer::compareTo);

        for(int i : foundWordsIndexes) {
            words.set(i, "<b>" + words.get(i) + "</b>");
        }

        index = foundWordsIndexes.get(0);
        beginning = Math.max(0, index - before);
        end = Math.min(words.size() - 1, index + after);

        for (int i = 1; i <= foundWordsIndexes.size(); i++) {
            if(i == foundWordsIndexes.size()) {
                snippetCollector.add(String.join("", words.subList(beginning, end)));
                break;
            }
            prevIndex = index;
            index = foundWordsIndexes.get(i);
            if(index - before <= prevIndex) {
                end = Math.min(words.size() - 1, index + after);
                continue;
            }
            snippetCollector.add(String.join("", words.subList(beginning, end)));
            beginning = Math.max(0, index - before);
            end = Math.min(words.size() - 1, index + after);
        }
        return String.join("...", snippetCollector);
    }

    public List<String> getNormalFormOfAWord(String word) {
        word = word.replaceAll("ё", "е");
        if (russianLuceneMorph.checkString(word) && !isRussianGarbage(russianLuceneMorph.getMorphInfo(word))) {
            return russianLuceneMorph.getNormalForms(word);
        } else if (englishLuceneMorph.checkString(word) && !isEnglishGarbage(englishLuceneMorph.getMorphInfo(word))) {
            return englishLuceneMorph.getNormalForms(word);
        } else if (word.chars().allMatch(Character::isDigit)){
            return Collections.singletonList(word);
        }
        return new ArrayList<>();
    }


    boolean isRussianGarbage(List<String> morphInfos) {
        for(String variant : morphInfos) {
            if (variant.contains(" СОЮЗ") || variant.contains(" МЕЖД") ||
                    variant.contains(" ПРЕДЛ") || variant.contains(" ЧАСТ")) {
                return true;
            }
        }
        return false;
    }
    boolean isEnglishGarbage (List<String> morphInfos) {
        for(String variant : morphInfos) {
            if (variant.contains(" CONJ") || variant.contains(" INT") ||
                    variant.contains(" PREP") || variant.contains(" PART") ||  variant.contains(" ARTICLE")) {
                return true;
            }
        }
        return false;
    }
}
