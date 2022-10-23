package com.ibapplications.niktoe.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FiveLetters2 {

    public static final Map<String, Set<String>> usableMap = new HashMap<>();

    public static final Path outPath = Paths.get("output.txt");

    public static final AtomicInteger wordCount = new AtomicInteger();

    public static void main(String[] args) throws IOException {
        if(Files.exists(outPath)) {
            Files.delete(outPath);
        }
        Files.createFile(outPath);
        Path p = Paths.get("words_alpha.txt");

        try(Stream<String> fileStream = Files.lines(p).parallel()) {
            List<String> words = fileStream.filter(FiveLetters2::removeDouble).toList();
            HashSet<Set<String>> sets = new HashSet<>();
            words.forEach(w -> sets.addAll(findSets(w, words)));
            System.out.println("Found " + sets.size());
        }

    }

    public static Set<Set<String>> findSets(String word, List<String> allWords) {
        System.out.println(MessageFormat.format("Handling word {0}", wordCount.incrementAndGet()));
        Function<String, Set<String>> matchingComputer =  (w) ->generateUsableList(w, allWords);

        Set<String> matching = usableMap.computeIfAbsent(word, matchingComputer);

        HashSet<Set<String>> result = new HashSet<>();
        int i = 0;
        for(String word2: matching) {
            System.out.println(MessageFormat.format("Matching work {0} out of {1}", i++, matching.size()));
            Set<String> matching2 = findMatching(word2, matching, matchingComputer);
            for(String word3: matching2) {

                Set<String> matching3 = findMatching(word3, matching2, matchingComputer);
                for(String word4: matching3) {
                    Set<String> matching4 = findMatching(word4, matching3, matchingComputer);
                    for (String word5 : matching4) {
                        result.add(Set.of(word, word2, word3, word4, word5));
                    }
                }
            }
        }
        return result;
    }

    private static Set<String> findMatching(String word, Set<String> limit, Function<String, Set<String>> matchingComputer) {
        Set<String> matching = new HashSet<>(usableMap.computeIfAbsent(word, matchingComputer));
        matching.retainAll(limit);
        return matching;
    }

    public static Set<String> generateUsableList(String word, List<String> allWords) {
        return allWords.parallelStream()
                .filter(l -> word.chars().allMatch(c -> l.indexOf(c) == -1)).collect(Collectors.toSet());
    }


    private static boolean removeDouble(String word) {
        return word.length() == 5 && word.chars().distinct().count() == 5;
    }
}
