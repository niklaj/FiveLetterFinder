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

public class FiveLetters6 {

    public static final Map<String, Set<String>> neighbours = new HashMap<>();

    public static final Path outPath = Paths.get("output.txt");

    public static final AtomicInteger wordCount = new AtomicInteger();

    public static void main(String[] args) throws IOException {
        if(Files.exists(outPath)) {
            Files.delete(outPath);
        }
        Files.createFile(outPath);
        Path p = Paths.get("words_alpha.txt");

        try(Stream<String> fileStream = Files.lines(p).parallel()) {
            List<String> words = fileStream.filter(FiveLetters6::removeDouble).toList();
            createNeighbours(words);
            for(String word: words) {
                analyseWord(word);
            }
            System.out.println("Complete");
        }

    }

    private static boolean removeDouble(String word) {
        return word.length() == 5 && word.chars().distinct().count() == 5;
    }

    private static void createNeighbours(List<String> words) {
        for(String word: words) {
            Set<String> localNeighbours = new HashSet<>();
            Set<Integer> letters = word.chars().boxed().collect(Collectors.toSet());
            for(String toCheck: words) {
                int checksum = toCheck.chars().filter(letters::contains).sum();
                if(checksum == 0) {
                    localNeighbours.add(toCheck);
                }
            }
            neighbours.put(word, localNeighbours);
        }
    }

    private static void analyseWord(String word) {
        System.out.println(MessageFormat.format("Handling word {0}", wordCount.incrementAndGet()));
        Set<Set<String>> result = new HashSet<>();
        Set<String> localNeighbours = neighbours.get(word);
        int i = 0;
        for(String j: localNeighbours) {
            System.out.println(MessageFormat.format("Looping word {0} of {1}", i++, localNeighbours.size()));
            Set<String> nij = new HashSet<>(neighbours.get(j));
            nij.retainAll(localNeighbours);
            for(String k: nij) {
                Set<String> nijk = new HashSet<>(neighbours.get(k));
                nijk.retainAll(nij);
                for(String l: nijk) {
                    Set<String> nijkl = new HashSet<>(neighbours.get(l));
                    nijkl.retainAll(nijk);
                    for(String r: nijkl) {
                        result.add(Set.of(word, j, k, l, r));
                    }
                }
            }
        }
    }
}
