package searchengine.services.search;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import java.io.IOException;
import java.util.*;

public class MakeSnippet {
    LuceneMorphology russianLuceneMorph;
    LuceneMorphology englishLuceneMorph;

    {
        try {
            russianLuceneMorph = new RussianLuceneMorphology();
            englishLuceneMorph = new EnglishLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSnippet(String text, List<String> searchQuery) {
        List<String> queryLocalCopy = new ArrayList<>(searchQuery);
        List<String> words = List.of(text.split("\\s"));
        ListIterator<String> iterator;
        List<Integer> foundWordsIndexes = new ArrayList<>();

        for (String word : words) {
            if (queryLocalCopy.isEmpty()) {
                break;
            }
            iterator = queryLocalCopy.listIterator();
            while (iterator.hasNext()) {
                List<String> wordNormalForm = new ArrayList<>(getNormalFormOfAWord(word.toLowerCase(Locale.ROOT)));
                wordNormalForm.retainAll(getNormalFormOfAWord(iterator.next()));
                if (wordNormalForm.isEmpty()) {
                    continue;
                }
                foundWordsIndexes.add(words.indexOf(word));
                iterator.remove();
            }
        }
        return constructSnippetWithHighlight(foundWordsIndexes, new ArrayList<>(words));
    }

//    public static void main(String[] args) {
//        String text = "карты памяти";
//        List<String> searchQuery = List.of("карты");
//        String getSnippet = getSnippet(text, searchQuery);
//        System.out.println(getSnippet);
//    }

    public static String constructSnippetWithHighlight(List<Integer> foundWordsIndexes, List<String> words) {
        List<String> snippetCollector = new ArrayList<>();
        int beginning, end, before, after, index, prevIndex;
        before = 12;
        after = 12;

        foundWordsIndexes.sort(Integer::compareTo);

        for (int i : foundWordsIndexes) {
            words.set(i, "<font color='#FF00FF'><b>" + words.get(i) + "</b></font>");
        }

        index = foundWordsIndexes.get(0);
        beginning = Math.max(0, index - before);
        end = Math.min(words.size() - 1, index + after);

        for (int i = 1; i <= foundWordsIndexes.size(); i++) {
            if (i == foundWordsIndexes.size()) {
                snippetCollector.add(String.join(" ", words.subList(beginning, end)));
                break;
            }
            prevIndex = index;
            index = foundWordsIndexes.get(i);
            if (index - before <= prevIndex) {
                end = Math.min(words.size() - 1, index + after);
                continue;
            }
            snippetCollector.add(String.join(" ", words.subList(beginning, end)));
            beginning = Math.max(0, index - before);
            end = Math.min(words.size() - 1, index + after);
        }
        return String.join("...", snippetCollector);
    }

    public List<String> getNormalFormOfAWord(String word) {
        word = word.replaceAll("ё", "е");
        if (russianLuceneMorph.checkString(word) && !serviceWords(russianLuceneMorph.getMorphInfo(word))) {
            return russianLuceneMorph.getNormalForms(word);
        } else if (word.chars().allMatch(Character::isDigit)) {
            return Collections.singletonList(word);
        }
        return new ArrayList<>();
    }


    boolean serviceWords(List<String> morphInfo) {
        for (String variant : morphInfo) {
            if (variant.contains(" СОЮЗ") || variant.contains(" МЕЖД") ||
                    variant.contains(" ПРЕДЛ") || variant.contains(" ЧАСТ")) {
                return true;
            }
        }
        return false;
    }
}
