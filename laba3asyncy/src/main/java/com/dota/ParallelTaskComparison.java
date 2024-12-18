package com.dota;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;

public class ParallelTaskComparison {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter directory path: ");
        String directoryPath = scanner.nextLine();
        System.out.print("Enter word or letter to search in file names: ");
        String searchTerm = scanner.nextLine();

        // Prepare tasks
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            System.out.println("Invalid directory path.");
            return;
        }

        File[] files = directory.listFiles();
        List<File> fileList = Arrays.asList(files);

        // Compare Work Stealing vs Work Dealing
        long startTimeStealing = System.nanoTime();
        int resultStealing = workStealing(fileList, searchTerm);
        long endTimeStealing = System.nanoTime();

        long startTimeDealing = System.nanoTime();
        int resultDealing = workDealing(fileList, searchTerm);
        long endTimeDealing = System.nanoTime();

        System.out.println("Work Stealing result: " + resultStealing);
        System.out.println("Time taken by Work Stealing: " + (endTimeStealing - startTimeStealing) / 1e6 + " ms");

        System.out.println("Work Dealing result: " + resultDealing);
        System.out.println("Time taken by Work Dealing: " + (endTimeDealing - startTimeDealing) / 1e6 + " ms");

        // Comparison
        if ((endTimeStealing - startTimeStealing) < (endTimeDealing - startTimeDealing)) {
            System.out.println("Work Stealing is faster.");
        } else {
            System.out.println("Work Dealing is faster.");
        }
    }

    // Work Stealing using ForkJoinPool
    public static int workStealing(List<File> files, String searchTerm) throws InterruptedException, ExecutionException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        List<RecursiveTask<Integer>> tasks = new ArrayList<>();

        for (File file : files) {
            tasks.add(new SearchTask(file, searchTerm));
        }

        // Fork tasks individually and wait for results
        for (RecursiveTask<Integer> task : tasks) {
            task.fork();
        }

        int count = 0;
        for (RecursiveTask<Integer> task : tasks) {
            count += task.join();
        }

        forkJoinPool.shutdown();
        return count;
    }

    // Work Dealing using ForkJoinPool
    public static int workDealing(List<File> files, String searchTerm) throws InterruptedException, ExecutionException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        List<RecursiveTask<Integer>> tasks = new ArrayList<>();

        // Split files into chunks and assign them evenly to threads
        int chunkSize = files.size() / 4;
        for (int i = 0; i < 4; i++) {
            int start = i * chunkSize;
            int end = (i + 1) * chunkSize;
            if (i == 3) {
                end = files.size(); // Handle last chunk
            }
            List<File> chunk = files.subList(start, end);
            tasks.add(new SearchTask(chunk, searchTerm));
        }

        // Fork tasks individually and wait for results
        for (RecursiveTask<Integer> task : tasks) {
            task.fork();
        }

        int count = 0;
        for (RecursiveTask<Integer> task : tasks) {
            count += task.join();
        }

        forkJoinPool.shutdown();
        return count;
    }

    // RecursiveTask for searching files
    static class SearchTask extends RecursiveTask<Integer> {
        private final List<File> files;
        private final String searchTerm;

        SearchTask(List<File> files, String searchTerm) {
            this.files = files;
            this.searchTerm = searchTerm;
        }

        SearchTask(File file, String searchTerm) {
            this.files = Collections.singletonList(file);
            this.searchTerm = searchTerm;
        }

        @Override
        protected Integer compute() {
            int count = 0;
            for (File file : files) {
                if (file.getName().contains(searchTerm)) {
                    count++;
                }
            }
            return count;
        }
    }
}
