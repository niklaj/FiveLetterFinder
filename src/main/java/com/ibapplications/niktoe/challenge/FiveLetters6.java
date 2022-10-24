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

    public static final long heapMaxSize = Runtime.getRuntime().maxMemory();

    public static void main(String[] args) throws IOException {
        System.out.println("Available memory(MB): " + heapMaxSize/1024/1026);
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
        Set<Integer>[] neighbours = createNeighboursNew(words);
        List<Set<String>> results = new ArrayList<>();
        for(int i = 0; i < neighbours.length;i++) {
        //for(int i = 0; i < 200;i++) {
            AtomicInteger deepth = new AtomicInteger();
            AtomicInteger checks = new AtomicInteger();
            Instant start = Instant.now();
            //System.out.println(MessageFormat.format("Handling word {0} of {1}", i, neighbours.length));
            Set<Integer> ni = new HashSet<>(neighbours[i]);
            System.out.println(MessageFormat.format("Handling word {0} of {1} searching through {2} options ({3})", i, neighbours.length, ni.size(), Runtime.getRuntime().freeMemory()/1024/1024));
            int index = i;
            ni.parallelStream().forEach(j -> {
                //System.out.println(MessageFormat.format("Looping word {0} of {1}", index++, ni.size()));
                if(index > j) return;
                Set<Integer> nij = new HashSet<>(ni);
                nij.retainAll(neighbours[j]);
                Set<Integer> nijk = new HashSet<>();
                Set<Integer> nijkl = new HashSet<>();
                for(Integer k: nij) {
                    if(j > k) continue;
                    checks.incrementAndGet();
                    deepth.getAndUpdate(v -> Math.max(v, 2));
                    nijk.clear();
                    nijk.addAll(nij);
                    nijk.retainAll(neighbours[k]);
                    for(Integer l: nijk) {
                        if(k > l) continue;
                        checks.incrementAndGet();
                        deepth.getAndUpdate(v -> Math.max(v, 3));
                        nijkl.clear();
                        nijkl.addAll(nijk);
                        nijkl.retainAll(neighbours[l]);
                        for(Integer r: nijkl) {
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

    private static void handleOld(List<String> words) {
        createNeighbours(words);
        for(String word: words) {
            analyseWord(word);
        }
        System.out.println("Complete");
    }

    private static Set<Integer>[] createNeighboursNew(List<String> words) {
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

    private static synchronized void write(List<Set<String>> groups) throws IOException {

        for(Set<String> group: groups) {
            Files.writeString(outPath, String.join("\t", group) + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
    }
}
