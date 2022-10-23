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

public class FiveLetters4 {
    private static Map<Integer, Set<String>> charList = new HashMap<>();

    public static final Path outPath = Paths.get("output.txt");

    public static final AtomicInteger wordCount = new AtomicInteger();

    private static final Set<Integer> allLetters = "abcdefghijklmnopqrstuvxyz".chars().boxed().collect(Collectors.toSet());

    public static void main(String[] args) throws IOException {
        if(Files.exists(outPath)) {
            Files.delete(outPath);
        }
        Files.createFile(outPath);
        Path p = Paths.get("words_alpha.txt");

        try(Stream<String> fileStream = Files.lines(p).parallel()) {
            List<String> words = fileStream.filter(FiveLetters4::removeDouble).toList();
            createCharMap(words);
            //Set<Set<String>> groups = new HashSet<>();
            //words.forEach(w -> groups.addAll(createSets(w)));
            System.out.println("Complete");
        }

    }

    private static boolean removeDouble(String word) {
        return word.length() == 5 && word.chars().distinct().count() == 5;
    }

    private static void createCharMap(List<String> words) {
        words.forEach(w -> {
            Set<Integer> letters = w.chars().boxed().collect(Collectors.toSet());
            remove(allLetters, letters).forEach(l -> charList.computeIfAbsent(l, i -> new HashSet<>()).add(w));
        });
    }

   /* private static Set<Set<String>> createSets(String word) {
        System.out.println(MessageFormat.format("Handling word {0}", wordCount.incrementAndGet()));
        Set<Set<String>> result = new HashSet<>();
        Set<Integer> letters = word.chars().boxed().collect(Collectors.toSet());
        Set<Integer> remaining = remove(charList.keySet(), letters);
        int i = 0;
        for (Integer letter1 : remaining) {
            Set<Integer> remaining2 = remove(remaining, letter1);
            for (Integer letter2 : remaining) {
                Set<Integer> remaining3 = remove(remaining2, letter2);
                for (Integer letter3 : remaining) {
                    Set<Integer> remaining4 = remove(remaining3, letter3);
                    for (Integer letter4 : remaining) {
                        Set<String> possibilities = charList.get(letter1)
                    }
                }
            }
        }
        return result;
    }*/

    private static Set<Integer> remove(Set<Integer> remaining, Set<Integer> currentLetters) {
        Set<Integer> result = new HashSet<>(remaining);
        result.removeAll(currentLetters);
        return result;
    }

    private static Set<Integer> remove(Set<Integer> remaining, Integer currentLetter) {
        Set<Integer> result = new HashSet<>(remaining);
        result.remove(currentLetter);
        return result;
    }


}
