package com.dota;

import java.util.concurrent.*;
import java.util.*;
import java.time.*;

public class MatrixMultiplication {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);

        // Введення розмірів матриць
        System.out.print("Enter number of rows for the first matrix: ");
        int m = scanner.nextInt(); // Рядки першої матриці
        System.out.print("Enter number of columns for the first matrix: ");
        int n = scanner.nextInt(); // Стовпці першої матриці

        System.out.print("Enter number of rows for the second matrix: ");
        int p = scanner.nextInt(); // Рядки другої матриці
        System.out.print("Enter number of columns for the second matrix: ");
        int q = scanner.nextInt(); // Стовпці другої матриці

        // Перевірка правильності розмірів для множення матриць
        if (n != p) {
            System.out.println("Matrix multiplication is not possible. The number of columns in the first matrix must be equal to the number of rows in the second matrix.");
            return;
        }

        // Введення діапазону для рандомних значень
        System.out.print("Enter the minimum value for random numbers: ");
        int minValue = scanner.nextInt();
        System.out.print("Enter the maximum value for random numbers: ");
        int maxValue = scanner.nextInt();

        // Генерація матриць
        int[][] matrix1 = generateRandomMatrix(m, n, minValue, maxValue);
        int[][] matrix2 = generateRandomMatrix(p, q, minValue, maxValue);

        System.out.println("\nMatrix 1:");
        printMatrix(matrix1);

        System.out.println("\nMatrix 2:");
        printMatrix(matrix2);

        // Початок вимірювання часу для Work Stealing
        long startTimeStealing = System.nanoTime();
        int[][] resultStealing = multiplyMatricesWorkStealing(matrix1, matrix2);
        long endTimeStealing = System.nanoTime();

        // Початок вимірювання часу для Work Dealing
        long startTimeDealing = System.nanoTime();
        int[][] resultDealing = multiplyMatricesWorkDealing(matrix1, matrix2);
        long endTimeDealing = System.nanoTime();

        // Виведення результатів для Work Stealing
        System.out.println("\nResultant Matrix (Work Stealing):");
        printMatrix(resultStealing);
        System.out.println("\nTime taken for Work Stealing multiplication: " + (endTimeStealing - startTimeStealing) / 1e6 + " ms");

        // Виведення результатів для Work Dealing
        System.out.println("\nResultant Matrix (Work Dealing):");
        printMatrix(resultDealing);
        System.out.println("\nTime taken for Work Dealing multiplication: " + (endTimeDealing - startTimeDealing) / 1e6 + " ms");

        // Порівняння часу
        if ((endTimeStealing - startTimeStealing) < (endTimeDealing - startTimeDealing)) {
            System.out.println("Work Stealing is faster.");
        } else {
            System.out.println("Work Dealing is faster.");
        }
    }

    // Генерація випадкової матриці
    public static int[][] generateRandomMatrix(int rows, int cols, int minValue, int maxValue) {
        Random rand = new Random();
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextInt((maxValue - minValue) + 1) + minValue;
            }
        }
        return matrix;
    }

    // Виведення матриці
    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int elem : row) {
                System.out.print(elem + "\t");
            }
            System.out.println();
        }
    }

    // Множення матриць за допомогою Work Stealing (паралельно)
    public static int[][] multiplyMatricesWorkStealing(int[][] matrix1, int[][] matrix2) throws InterruptedException, ExecutionException {
        int m = matrix1.length;
        int n = matrix1[0].length;
        int p = matrix2.length;
        int q = matrix2[0].length;

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        int[][] result = new int[m][q];
        List<RecursiveTask<Void>> tasks = new ArrayList<>();

        // Розподіл задач для обчислення елементів результатної матриці
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < q; j++) {
                int row = i;
                int col = j;
                tasks.add(new MultiplyTask(matrix1, matrix2, result, row, col));
            }
        }

        // Виконання задач у пулі потоків через fork
        for (RecursiveTask<Void> task : tasks) {
            task.fork();
        }

        // Очікування на завершення всіх задач
        for (RecursiveTask<Void> task : tasks) {
            task.join();
        }

        // Закриття пулу потоків
        forkJoinPool.shutdown();

        return result;
    }

    // Множення матриць за допомогою Work Dealing (паралельно)
    public static int[][] multiplyMatricesWorkDealing(int[][] matrix1, int[][] matrix2) throws InterruptedException, ExecutionException {
        int m = matrix1.length;
        int n = matrix1[0].length;
        int p = matrix2.length;
        int q = matrix2[0].length;

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        int[][] result = new int[m][q];
        List<RecursiveTask<Void>> tasks = new ArrayList<>();

        // Розподіл задач на рівні частини (chunk)
        int chunkSize = m / 4;  // Припустимо 4 потоки для простоти
        for (int i = 0; i < 4; i++) {
            int start = i * chunkSize;
            int end = (i == 3) ? m : start + chunkSize;

            tasks.add(new MultiplyTaskWorkDealing(matrix1, matrix2, result, start, end));
        }

        // Виконання задач у пулі потоків через fork
        for (RecursiveTask<Void> task : tasks) {
            task.fork();
        }

        // Очікування на завершення всіх задач
        for (RecursiveTask<Void> task : tasks) {
            task.join();
        }

        // Закриття пулу потоків
        forkJoinPool.shutdown();

        return result;
    }

    // Підклас RecursiveTask для обчислення елементів матриці (Work Stealing)
    static class MultiplyTask extends RecursiveTask<Void> {
        private final int[][] matrix1;
        private final int[][] matrix2;
        private final int[][] result;
        private final int row;
        private final int col;

        MultiplyTask(int[][] matrix1, int[][] matrix2, int[][] result, int row, int col) {
            this.matrix1 = matrix1;
            this.matrix2 = matrix2;
            this.result = result;
            this.row = row;
            this.col = col;
        }

        @Override
        protected Void compute() {
            int sum = 0;
            for (int k = 0; k < matrix1[0].length; k++) {
                sum += matrix1[row][k] * matrix2[k][col];
            }
            result[row][col] = sum;
            return null;
        }
    }

    // Підклас RecursiveTask для обчислення елементів матриці (Work Dealing)
    static class MultiplyTaskWorkDealing extends RecursiveTask<Void> {
        private final int[][] matrix1;
        private final int[][] matrix2;
        private final int[][] result;
        private final int start;
        private final int end;

        MultiplyTaskWorkDealing(int[][] matrix1, int[][] matrix2, int[][] result, int start, int end) {
            this.matrix1 = matrix1;
            this.matrix2 = matrix2;
            this.result = result;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Void compute() {
            for (int i = start; i < end; i++) {
                for (int j = 0; j < matrix2[0].length; j++) {
                    int sum = 0;
                    for (int k = 0; k < matrix1[0].length; k++) {
                        sum += matrix1[i][k] * matrix2[k][j];
                    }
                    result[i][j] = sum;
                }
            }
            return null;
        }
    }
}
