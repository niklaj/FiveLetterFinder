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

public class FiveLetters3 {
    private static Map<Integer, Set<String>> charList = new HashMap<>();

    public static final Path outPath = Paths.get("output.txt");

    public static final AtomicInteger wordCount = new AtomicInteger();

    public static void main(String[] args) throws IOException {
        if(Files.exists(outPath)) {
            Files.delete(outPath);
        }
        Files.createFile(outPath);
        Path p = Paths.get("words_alpha.txt");

        try(Stream<String> fileStream = Files.lines(p).parallel()) {
            List<String> words = fileStream.filter(FiveLetters3::removeDouble).toList();
            createCharMap(words);
            Set<Set<String>> groups = new HashSet<>();
            words.forEach(w -> groups.addAll(createSets(w)));
            System.out.println("Complete");
        }

    }

    private static boolean removeDouble(String word) {
        return word.length() == 5 && word.chars().distinct().count() == 5;
    }

    private static void createCharMap(List<String> words) {
        words.forEach(w -> w.chars().forEach(c -> charList.computeIfAbsent(c, i -> new HashSet<>()).add(w)));
    }

    private static Set<Set<String>> createSets(String word) {
        System.out.println(MessageFormat.format("Handling word {0}", wordCount.incrementAndGet()));
        Set<Set<String>> result = new HashSet<>();
        Set<Integer> letters = word.chars().boxed().collect(Collectors.toSet());
        Set<Integer> remaining = remove(charList.keySet(), letters);
        int i = 0;
        for (Integer letter1 : remaining) {
            System.out.println(MessageFormat.format("Handling letter {0} out of {1}", i++, remaining.size()));
            for (String word2 : charList.get(letter1)) {
                Set<Integer> letters2 = word2.chars().boxed().collect(Collectors.toSet());
                Set<Integer> remaining2 = remove(remaining, letters2);
                if (remaining2.size() == 16) {
                    int j = 0;
                    for (Integer letter2 : remaining2) {
                        System.out.println(MessageFormat.format("Handling letter2 {0} out of {1}", j++, remaining2.size()));
                        for (String word3 : charList.get(letter2)) {
                            Set<Integer> letters3 = word3.chars().boxed().collect(Collectors.toSet());
                            Set<Integer> remaining3 = remove(remaining2, letters3);
                            if (remaining3.size() == 11) {
                                for (Integer letter3 : remaining3) {
                                    for (String word4 : charList.get(letter3)) {
                                        Set<Integer> letters4 = word4.chars().boxed().collect(Collectors.toSet());
                                        Set<Integer> remaining4 = remove(remaining3, letters4);
                                        if (remaining4.size() == 6) {
                                            for (Integer letter4 : remaining4) {
                                                for (String word5 : charList.get(letter4)) {
                                                    Set<Integer> letters5 = word5.chars().boxed().collect(Collectors.toSet());
                                                    Set<Integer> remaining5 = remove(remaining4, letters5);
                                                    if (remaining5.size() == 1) {
                                                        result.add(Set.of(word, word2, word3, word4, word5));
                                                    }
                                                }
                                            }
                                        }

                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static Set<Integer> remove(Set<Integer> remaining, Set<Integer> currentLetters) {
        Set<Integer> result = new HashSet<>(remaining);
        result.removeAll(currentLetters);
        return result;
    }


}
