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
            filtered.forEach(FiveLetters::handleWord);
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
        System.out.println(MessageFormat.format("Handling word {0}", wordCount.incrementAndGet()));
        try {
            Set<String> allPossible = compatibleMap.get(word);
            if(allPossible != null) {
                allPossible.removeAll(handled);
                List<String> possible = allPossible.stream()
                        .filter(w -> handleWord(w, allPossible))
                        .toList();

                if (possible.size() >= 4) {
                    write(word, possible);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean handleWord(String word, Set<String> words) {
        return compatibleMap.containsKey(word) && compatibleMap.get(word).stream().filter(words::contains).count() > 2;
    }


    private static synchronized void write(String main, List<String> words) throws IOException {

        Set<Set<String>> subSets = getSubsets(words, 4);
        for(int i = 0; i+4 < subSets.size();i++) {
            List<String> toWrite = new ArrayList<>();
            toWrite.add(main);
            toWrite.addAll(words.subList(i, i+4));
            Files.writeString(outPath, main + "\t" +  String.join("\t", toWrite) + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
    }

    public static Set<Set<String>> getSubsets(Collection<String> initial, int size) {
        if(initial.size() == size) {
            return Set.of(new HashSet<>(initial));
        }
        return initial.stream().map(item -> {
                    Set<String> clone = new HashSet<>(initial);
                    clone.remove(item);
                    return clone;
                }).map(group -> getSubsets(group, size))
                .reduce(new HashSet<>(), (x, y) -> {
                    x.addAll(y);
                    return x;
                });
    }

}
