package com.ibapplications.niktoe.challenge;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetSolutionTester {

    public static void main(String[] args) {
        Random r = new Random();

        Set<Integer> large = r.ints(1000, 0, 10000).boxed().collect(Collectors.toSet());
        Set<Integer> small = r.ints(100, 0, 10000).boxed().collect(Collectors.toSet());
        HashSet<Integer> hashLarge = new HashSet<>(large);
        HashSet<Integer> hashSmall = new HashSet<>(small);
        TreeSet<Integer> treeLarge = new TreeSet<>(large);
        TreeSet<Integer> treeSmall = new TreeSet<>(small);
        LinkedHashSet<Integer> linkedHashLarge = new LinkedHashSet<>(large);
        LinkedHashSet<Integer> linkedHashSmall = new LinkedHashSet<>(small);


        Set<Integer> correctResult = new HashSet<>(small);
        correctResult.retainAll(large);

        final int[] repetitions = IntStream.range(0, 100000).toArray();

        Set<Integer> result = null;
        Instant start = Instant.now();
        for(int i: repetitions) {
            result = retainLargeFirst(hashLarge, hashSmall);
        }
        Duration duration = Duration.between(start, Instant.now());
        boolean correct = correctResult.equals(result);
        System.out.printf("(Hash)retainLargeFirst[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = retainSmallFirst(hashLarge, hashSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(Hash)retainSmallFirst[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = streamLarge(hashLarge, hashSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(Hash)streamLarge[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = streamSmall(hashLarge, hashSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(Hash)streamSmall[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = retainLargeFirst(treeLarge, treeSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(tree)retainLargeFirst[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = retainSmallFirst(treeLarge, treeSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(tree)retainSmallFirst[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = streamLarge(treeLarge, treeSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(tree)streamLarge[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = streamSmall(treeLarge, treeSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(tree)streamSmall[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = retainSmallFirst(linkedHashLarge, linkedHashSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(linkedhash)retainSmallFirst[%s] completed in %s\n", correct, duration);

        //----------------------------------------------------


        start = Instant.now();
        for(int i: repetitions) {
            result = streamSmall(linkedHashLarge, linkedHashSmall);
        }
        duration = Duration.between(start, Instant.now());
        correct = correctResult.equals(result);
        System.out.printf("(linkedhash)streamSmall[%s] completed in %s\n", correct, duration);

    }




    public static Set<Integer> retainLargeFirst(Set<Integer> large, Set<Integer> small) {
        Set<Integer> result = new HashSet<>(large);
        result.retainAll(small);
        return result;
    }

    public static Set<Integer> retainSmallFirst(Set<Integer> large, Set<Integer> small) {
        Set<Integer> result = new HashSet<>(small);
        result.retainAll(large);
        return result;
    }

    public static Set<Integer> streamLarge(Set<Integer> large, Set<Integer> small) {
        return large.stream().filter(small::contains).collect(Collectors.toSet());
    }

    public static Set<Integer> streamSmall(Set<Integer> large, Set<Integer> small) {
        return small.stream().filter(large::contains).collect(Collectors.toSet());
    }

}
