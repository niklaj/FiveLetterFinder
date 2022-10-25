package com.ibapplications.niktoe.challenge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FiveLetters6 {

    public static final Map<String, Set<String>> neighbours = new HashMap<>();

    public static final Path outPath = Paths.get("output.txt");

    public static final AtomicInteger wordCount = new AtomicInteger();


    public static void main(String[] args) throws IOException {
        System.out.println("Available memory(MB): " + Runtime.getRuntime().maxMemory()/1024/1026);
        if(Files.exists(outPath)) {
            Files.delete(outPath);
        }
        Files.createFile(outPath);
        Path p = Paths.get("words_alpha.txt");

        try(Stream<String> fileStream = Files.lines(p).parallel()) {
            List<String> words = fileStream.filter(FiveLetters6::removeDouble).toList();
            handle(words);
        }

    }

    private static boolean removeDouble(String word) {
        return word.length() == 5 && word.chars().distinct().count() == 5;
    }

    private static void handle(List<String> words) throws IOException {
        Instant methodStart = Instant.now();
        Set<Integer>[] neighbours = createNeighbours(words);
        List<Set<String>> results = new ArrayList<>();
        for(int i = 0; i < neighbours.length;i++) {
        //for(int i = 0; i < 200;i++) {
            AtomicInteger deepth = new AtomicInteger();
            AtomicInteger checks = new AtomicInteger();
            Instant start = Instant.now();
            Set<Integer> ni = new HashSet<>(neighbours[i]);
            System.out.println(MessageFormat.format("Handling word {0} of {1} searching through {2} options ({3})", i, neighbours.length, ni.size(), Runtime.getRuntime().freeMemory()/1024/1024));
            int index = i;
            ni.parallelStream().forEach(j -> {
                //System.out.println(MessageFormat.format("Looping word {0} of {1}", index++, ni.size()));
                if(index > j) return;
                Set<Integer> nij = ni.stream().filter(neighbours[j]::contains).collect(Collectors.toSet());
                for(Integer k: nij) {
                    if(j > k) continue;
                    checks.incrementAndGet();
                    deepth.getAndUpdate(v -> Math.max(v, 2));
                    Set<Integer> nijk = nij.stream().filter(neighbours[k]::contains).collect(Collectors.toSet());
                    for(Integer l: nijk) {
                        if(k > l) continue;
                        checks.incrementAndGet();
                        deepth.getAndUpdate(v -> Math.max(v, 3));
                        Set<Integer> nijkl = nijk.stream().filter(neighbours[l]::contains).collect(Collectors.toSet());
                        for(Integer r: nijkl) {
                            if(l > r) continue;
                            checks.incrementAndGet();
                            deepth.getAndUpdate(v -> Math.max(v, 4));
                            results.add(Set.of(words.get(index), words.get(j), words.get(k), words.get(l), words.get(r)));
                        }
                    }
                }
            });

            System.out.println("Reached deepth: " + deepth + " with checks: " + checks + " in " + Duration.between(start, Instant.now()));
        }
        write(results);
        System.out.printf("Total execution time %s", Duration.between(methodStart, Instant.now()));
    }

    private static Set<Integer>[] createNeighbours(List<String> words) {
        Set<Integer>[] neighbours = new Set[words.size()];
        for(int i = 0; i < words.size(); i++) {
            Set<Integer> localNeighBours = new HashSet<>();
            Set<Integer> letters = words.get(i).chars().boxed().collect(Collectors.toSet());
            for(int j = 0; j < words.size();j++) {
                String toCheck = words.get(j);
                int checksum = toCheck.chars().filter(letters::contains).sum();
                if(checksum == 0) {
                    localNeighBours.add(j);
                }
            }
            neighbours[i] = localNeighBours;
        }
        return neighbours;
    }

    private static synchronized void write(List<Set<String>> groups) throws IOException {
        Instant start = Instant.now();
        for(Set<String> group: groups) {
            Files.writeString(outPath, String.join("\t", group) + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
        System.out.printf("Completed write in %s", Duration.between(start, Instant.now()));
    }
}
