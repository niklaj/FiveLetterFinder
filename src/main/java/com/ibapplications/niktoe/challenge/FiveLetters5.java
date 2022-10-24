package com.ibapplications.niktoe.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FiveLetters5 {
    private static Map<Integer, Set<String>> negativeList = new HashMap<>();
    private static Map<Integer, Set<String>> postiveList = new HashMap<>();

    public static final Path outPath = Paths.get("output.txt");

    public static final AtomicInteger wordCount = new AtomicInteger();

    private static final Set<Integer> allLetters = "abcdefghijklmnopqrstuvwxyz".chars().boxed().collect(Collectors.toSet());

    public static void main(String[] args) throws IOException {
        if(Files.exists(outPath)) {
            Files.delete(outPath);
        }
        Files.createFile(outPath);
        Path p = Paths.get("words_alpha.txt");

        try(Stream<String> fileStream = Files.lines(p).parallel()) {
            List<String> words = fileStream.filter(FiveLetters5::removeDouble).toList();
            createCharMap(words);
            for (Integer letter : allLetters) {
                handleLetter(letter);
            }
            System.out.println("Complete");
        }

    }

    private static boolean removeDouble(String word) {
        return word.length() == 5 && word.chars().distinct().count() == 5;
    }

    private static void createCharMap(List<String> words) {
        words.forEach(w -> {
            Set<Integer> letters = w.chars().boxed().collect(Collectors.toSet());
            letters.forEach(l -> postiveList.computeIfAbsent(l, i -> new HashSet<>()).add(w));
            remove(allLetters, letters).forEach(l -> negativeList.computeIfAbsent(l, i -> new HashSet<>()).add(w));
        });
    }

    private static void handleLetter(Integer letter) {
        Set<Set<String>> results = new HashSet<>();

        Set<String> canBe = negativeList.get(letter);
        Set<String> cantBe = postiveList.get(letter);

        Set<Integer> letters1 = remove(allLetters, letter);
        for(Integer letter1: letters1) {
            Set<String> words1 = remove(postiveList.get(letter1), cantBe);
            words1.retainAll(canBe);
            for(String word1: words1) {
                System.out.println(MessageFormat.format("Handling word {0} of {1}", wordCount.incrementAndGet(), words1.size()));
                Set<Integer> letters2 = removeLetters(letters1, word1);
                Set<String> canBe2 = remove(canBe, word1);
                for(Integer letter2: letters2) {
                    Set<String> words2 = limit(canBe2, postiveList.get(letter2));
                    for(String word2: words2) {
                        Set<Integer> letters3 = removeLetters(letters2, word2);
                        Set<String> canBe3 = remove(canBe2, word2);
                        for(Integer letter3: letters3) {
                            Set<String> words3 = limit(canBe3, postiveList.get(letter3));
                            for(String word3: words3) {
                                Set<String> canBe4 = remove(canBe3, word3);
                                for(String word4: canBe4) {
                                    Set<String> canBe5 = remove(canBe4, word4);
                                    for(String word5: canBe5) {
                                        results.add(Set.of(word1, word2, word3, word4, word5));
                                    }
                                }
                            }
                        }

                    }
                }

            }
        }
    }


    private static <T> Set<T> remove(Set<T> remaining, Set<T> currentLetters) {
        Set<T> result = new HashSet<>(remaining);
        result.removeAll(currentLetters);
        return result;
    }

    private static Set<String> remove(Set<String> remaining, String word ) {
        Set<String> result = new HashSet<>(remaining);
        word.chars().forEach(c -> {
            result.removeAll(postiveList.get(c));
        });
        return result;
    }

    private static Set<String> limit(Set<String> canBe, Set<String> currentLetter) {
        Set<String> result = new HashSet<>(canBe);
        result.removeAll(currentLetter);
        return result;
    }
    private static Set<String> remove(Set<String> remaining, Set<String> currentLetters, String... words ) {
        Set<String> result = new HashSet<>(remaining);
        result.removeAll(currentLetters);
        for(String word: words) {
            word.chars().forEach(c -> {
                result.removeAll(postiveList.get(c));
            });
        }
        return result;
    }

    private static Set<Integer> removeLetters(Set<Integer> remaining, String currentWord) {
        Set<Integer> result = new HashSet<>(remaining);
        currentWord.chars().forEach(result::remove);
        return result;
    }

    private static Set<Integer> remove(Set<Integer> remaining, Integer currentLetter) {
        Set<Integer> result = new HashSet<>(remaining);
        result.remove(currentLetter);
        return result;
    }


}
