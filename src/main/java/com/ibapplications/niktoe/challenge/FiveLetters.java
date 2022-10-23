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
    public static final Set<WordSet> wordTubles = new HashSet<>();

    public static void main(String[] args) throws IOException {
        if(Files.exists(outPath)) {
            Files.delete(outPath);
        }
        Files.createFile(outPath);
        Path p = Paths.get("words_alpha.txt");
        try(Stream<String> fileStream = Files.lines(p).parallel()) {
            List<String> words = fileStream.filter(FiveLetters::removeDouble).toList();
            List<String> filtered = words.parallelStream().filter(word -> processMatched(word, words)).toList();
            AtomicInteger i = new AtomicInteger();
            filtered.forEach(w -> {
                System.out.println(MessageFormat.format("Creating toubles for word {0} out of {1}", i.incrementAndGet(), filtered.size()));
                createWordTouples(w, compatibleMap.get(w));
            });
            processTouples();
            wordCount.set(0);
            Set<Set<String>> sets = getSets();
            write(sets);
            /*filtered.forEach(w -> {
                System.out.println(MessageFormat.format("Handling word {0} out of {1}", wordCount.incrementAndGet(), filtered.size()));
                handleWord(w);
            });*/
        }
    }

    private static boolean removeDouble(String word) {
        return word.length() == 5 && word.chars().distinct().count() == 5;
    }

    private static boolean processMatched(String word, List<String> allWords) {
        System.out.println(MessageFormat.format("Matching for words of {0} out of {1}", wordCount.incrementAndGet(), allWords.size()));
        List<String> compatible = allWords.parallelStream()
                .filter(l -> word.chars().allMatch(c -> l.indexOf(c) == -1))
                .toList();
        if (compatible.size() >= 4) {
            compatibleMap.put(word, new HashSet<>(compatible));
            return true;
        }
        return false;
    }

    private static void createWordTouples(String word, Set<String> matched) {
        List<WordSet> wordSets = matched.stream()
                .map(m -> WordSet.create(word, m, matched, compatibleMap.get(m)))
                .filter(WordSet::hasIntersection)
                .toList();
        wordTubles.addAll(wordSets);

    }

    private static void processTouples() {
        for(WordSet wordSet: wordTubles) {
            for(WordSet wordSetInner: wordTubles) {
                wordSet.checkOther(wordSetInner);
            }
        }
    }

    public static Set<Set<String>> getSets() {
        Set<Set<String>> allSets = new HashSet<>();
        int i = 0;
        for(WordSet wordSet: wordTubles) {
            System.out.println(MessageFormat.format("Looking at set {0} out of {1}", i++, wordTubles.size()));
            for(Map.Entry<WordSet, Set<String>> matched: wordSet.matchedSets.entrySet()) {
                for(String last: matched.getValue()) {
                    allSets.add(Set.of(wordSet.first, wordSet.second, matched.getKey().first, matched.getKey().second, last));
                }
            }
        }
        return allSets;
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

    public record WordSet(
            String first,
            String second,
            int firstHash,
            int secondHash,
            Set<String> matched,
            HashMap<WordSet, Set<String>> matchedSets
    ) {
        public static WordSet create(String first, String second, Set<String> firstMatched, Set<String> secondMatched) {
            String actual1 = first.compareTo(second) > 0 ? first : second;
            String actual2 = first.compareTo(second) > 0 ? second : first;
            Set<String> intersection = new HashSet<>(firstMatched);
            intersection.retainAll(secondMatched);
            return new WordSet(actual1, actual2, actual1.hashCode(), actual2.hashCode(), intersection, new HashMap<>());
        }

        public boolean hasIntersection() {
            return !matched.isEmpty();
        }

        public void checkOther(WordSet wordSet) {
            if(firstHash != wordSet.firstHash && firstHash != wordSet.secondHash && secondHash != wordSet.firstHash && secondHash != wordSet.secondHash) {
                if(!this.equals(wordSet) && !matchedSets.containsKey(wordSet)) {
                    Set<String> intersection = new HashSet<>(matched);
                    intersection.retainAll(wordSet.matched);
                    if(!intersection.isEmpty()) {
                        matchedSets.put(wordSet, intersection);
                        wordSet.matchedSets.put(this, intersection);
                    }
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WordSet wordSet = (WordSet) o;
            return first.equals(wordSet.first) && second.equals(wordSet.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }

}
