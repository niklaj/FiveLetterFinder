package com.ibapplications.niktoe.challenge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FiveLetters {

    private static final Set<String> handled = Collections.synchronizedSet(new HashSet<>());

    public static final AtomicInteger wordCount = new AtomicInteger();

    public static final Path outPath = Paths.get("output.txt");

    public static final Map<String, Set<String>> compatibleMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if(Files.exists(outPath)) {
            Files.delete(outPath);
        }
        Files.createFile(outPath);
        Path p = Paths.get("words_alpha.txt");
        try(Stream<String> fileStream = Files.lines(p).parallel()) {
            List<String> words = fileStream.filter(FiveLetters::removeDouble).toList();
            List<String> filtered = words.parallelStream().filter(word -> preprocessWords(word, words)).toList();
            wordCount.set(0);
            filtered.forEach(w -> {
                System.out.println(MessageFormat.format("Handling word {0} out of {1}", wordCount.incrementAndGet(), filtered.size()));
                handleWord(w);
            });
        }
    }

    private static boolean removeDouble(String word) {
        return word.length() == 5 && word.chars().distinct().count() == 5;
    }

    private static boolean preprocessWords(String word, List<String> allWords) {
        System.out.println(MessageFormat.format("Preprocessing word {0} out of {1}", wordCount.incrementAndGet(), allWords.size()));
        List<String> compatible = allWords.parallelStream()
                .filter(l -> word.chars().allMatch(c -> l.indexOf(c) == -1))
                .toList();
        if (compatible.size() >= 4) {
            compatibleMap.put(word, new HashSet<>(compatible));
            return true;
        }
        return false;
    }

    private static void handleWord(String word) {
        try {
            Set<String> allPossible = compatibleMap.get(word);
            if(allPossible != null) {
                allPossible.removeAll(handled);
                int i = 0;
                Set<Set<String>> allResults = new HashSet<>();
                for(String subWord : allPossible) {
                    //System.out.println(MessageFormat.format("Handling subword {0} out of {1}", i++, allPossible.size()));
                    allResults.addAll(findSubsets(subWord, allPossible, 0));
                }
                /*Set<Set<String>> sets = allPossible.stream()
                        .flatMap(w -> findSubsets(w, allPossible, 0).stream())
                        .collect(Collectors.toSet());*/

                if(!allResults.isEmpty()) {
                    write(allResults);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<Set<String>> findSubsets(String word, Set<String> words, int index) {
        //System.out.println("Search level " + index);
        if(!compatibleMap.containsKey(word)) {
            return Set.of();
        }
        if(index == 4) {
            System.out.println("Found a set");
            return Set.of(new HashSet<>(Set.of(word)));
        }
        Set<String> compatible = compatibleMap.get(word).stream().filter(words::contains).collect(Collectors.toSet());

        Set<Set<String>> compatibleSets = compatible.stream().flatMap(s -> findSubsets(s, compatible, index+1).stream()).collect(Collectors.toSet());
        compatibleSets.forEach(s -> s.add(word));
        return compatibleSets;
    }

    private static synchronized void write(Set<Set<String>> groups) throws IOException {

        for(Set<String> group: groups) {
            Files.writeString(outPath, String.join("\t", group) + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
    }

}
