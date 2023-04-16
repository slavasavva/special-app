package searchengine.services.search;

import searchengine.services.LemmaFinder;

import java.io.IOException;
import java.util.*;

public class MakeSnippet {
    private LemmaFinder lemmaFinder;

    {
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSnippet(String text, List<String> searchQuery) {
        List<String> query = new ArrayList<>(searchQuery);
        List<String> words = List.of(text.split("\\s"));
        ListIterator<String> iterator;
        List<Integer> foundWordsIndexes = new ArrayList<>();

        for (String word : words) {
            if (query.isEmpty()) {
                break;
            }
            iterator = query.listIterator();
            while (iterator.hasNext()) {
                List<String> wordNormalForm = new ArrayList<>(lemmaFinder.getLemmaSet(word.toLowerCase(Locale.ROOT)));
                wordNormalForm.retainAll(lemmaFinder.getLemmaSet(iterator.next()));
                if (wordNormalForm.isEmpty()) {
                    continue;
                }
                foundWordsIndexes.add(words.indexOf(word));
                iterator.remove();
            }
        }
        return makeSnippetWithColorWords(foundWordsIndexes, new ArrayList<>(words));
    }

    public static String makeSnippetWithColorWords(List<Integer> foundWordsIndexes, List<String> words) {
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
}
