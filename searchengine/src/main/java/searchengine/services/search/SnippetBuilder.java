package searchengine.services.search;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class SnippetBuilder {
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


    public  String getSnippet(String text, List<String> searchQuery) {
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
//        String text = "Интернет-магазин PlayBack.ru PlayBack.ru 5 минут от метро ВДНХ   8(495)143-77-71 пн-пт: c 11 до 20 сб-вс: с 11 до 18   Возникла проблема? Напишите нам!   Корзина пуста Каталог Смартфоны Чехлы для смартфонов Xiaomi Защитные стекла для смартфонов Xiaomi Чехлы для Huawei/Honor Чехлы для смартфонов Samsung Защитные стекла для смартфонов Samsung Планшеты Зарядные устройства и кабели Держатели для смартфонов Автодержатели Носимая электроника Наушники и колонки Гаджеты Xiaomi Запчасти для телефонов Чехлы для планшетов Аксессуары для фото-видео Чехлы для смартфонов Apple Товары для автомобилистов USB Флеш-накопители Товары для детей Защитные стекла для смартфонов Realme Чехлы для смартфонов Realme Карты памяти Защитные стекла для планшетов Защитные стекла для смартфонов Доставка Самовывоз Оплата Гарантия и обмен Контакты Смартфон Samsung Galaxy A23 (SM-A235F/DS) 6/128 ГБ Global, голубой 17450р. Купить Смартфон Xiaomi Redmi 10A 2/32 ГБ RU, графитовый серый 5970р. Купить Смартфон Xiaomi Redmi 10A 2/32 ГБ RU, серебристый 5970р. Купить Смартфон Xiaomi Redmi 10A 2/32 ГБ RU, синий 5970р. Купить Смартфон Xiaomi Redmi 10A 3/64 ГБ Global, серебристый 7980р. Купить Смартфон Xiaomi Redmi 10A 3/64 ГБ Global, синий 7980р. Купить Смартфон Xiaomi Redmi 9A 2/32 ГБ Global, зеленый 4400р. Купить Смартфон Xiaomi Redmi 9A 2/32 ГБ RU, зеленый 4900р. Купить Смартфон Xiaomi Redmi 9A 2/32 ГБ RU, ледяная синева 4900р. Купить Смартфон Xiaomi Redmi 9C 4/128 ГБ (без NFC) Global, фиолетовый 9800р. Купить Смартфон Xiaomi Redmi 9C NFC 4/128 ГБ Global, черный 10780р. Купить Смартфон Xiaomi Redmi Note 10 Pro 8/128 ГБ RU, Glacier Blue 20230р. Купить Смартфон Xiaomi Redmi Note 10 Pro 8/128GB RU, Gradient Bronze 20230р. Купить Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, серый оникс 20500р. Купить Смартфон Xiaomi Redmi Note 10S 6/128 ГБ Global (без NFC), серый оникс 13490р. Купить Смартфон Xiaomi Redmi Note 10S NFC 6/128 ГБ RU, серый оникс 13890р. Купить Смартфон Xiaomi Redmi Note 10T 4/128 ГБ RU, Nighttime Blue 13900р. Купить Смартфон Xiaomi Redmi Note 11 Pro 5G 6/128 ГБ Global, графитовый серый 21650р. Купить Информация Наши спецпредложения Доставка Оплата Гарантия Контакты Положение о конфиденциальности и защите персональных данных +7(495)143-77-71    График работы: пн-пт: c 11-00 до 20-00 сб-вс: с 11-00 до 18-00 Наш адрес: Москва, Звездный бульвар, 10, строение 1, 2 этаж, офис 10. 2005-2023 ©Интернет магазин PlayBack.ru Наверх";
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
            words.set(i, "<b>" + words.get(i) + "</b>");
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

    public  List<String> getNormalFormOfAWord(String word) {
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
        for (String variant : morphInfos) {
            if (variant.contains(" СОЮЗ") || variant.contains(" МЕЖД") ||
                    variant.contains(" ПРЕДЛ") || variant.contains(" ЧАСТ")) {
                return true;
            }
        }
        return false;
    }

    boolean isEnglishGarbage(List<String> morphInfos) {
        for (String variant : morphInfos) {
            if (variant.contains(" CONJ") || variant.contains(" INT") ||
                    variant.contains(" PREP") || variant.contains(" PART") || variant.contains(" ARTICLE")) {
                return true;
            }
        }
        return false;
    }
}
