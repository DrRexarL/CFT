package com.example.filedatafilter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class FileDataFilterUtil {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^-?\\d*\\.\\d+([eE][-+]?\\d+)?$");

        // Метод для генерации тестовых файлов
        private static void generateTestFiles() {
            try {
                // Создаем и заполняем in1.txt
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("in1.txt"))) {
                    writer.write("123\n456\n789\n");
                    writer.write("12.34\n56.78\n");
                    writer.write("Hello\nWorld\n");
                }

                // Создаем и заполняем in2.txt
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("in2.txt"))) {
                    writer.write("987\n654\n321\n");
                    writer.write("87.65\n43.21\n");
                    writer.write("Goodbye\nUniverse\n");
                }

                System.out.println("Созданные тестовые файлы in1.txt и in2.txt");

            } catch (IOException e) {
                System.err.println("Ошибка при генерации тестовых файлов: " + e.getMessage());
            }
        }
    public static void main(String[] args) {
        generateTestFiles();  // Генерация тестовых файлов при каждом запуске
        List<String> inputFiles = new ArrayList<>();
        String outputPath = ".";
        String prefix = "";
        boolean appendMode = false;
        boolean shortStats = false;
        boolean fullStats = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    outputPath = args[++i];
                    break;
                case "-p":
                    prefix = args[++i];
                    break;
                case "-a":
                    appendMode = true;
                    break;
                case "-s":
                    shortStats = true;
                    break;
                case "-f":
                    fullStats = true;
                    break;
                default:
                    inputFiles.add(args[i]);
                    break;
            }
        }

        System.out.println("Входные файлы: " + inputFiles);
        System.out.println("Выходной путь: " + outputPath);
        System.out.println("Префикс: " + prefix);
        System.out.println("Append mode: " + appendMode);
        System.out.println("Краткая статистика: " + shortStats);
        System.out.println("Полная статистика: " + fullStats);

        Map<String, List<String>> dataMap = new HashMap<>();
        dataMap.put("integers", new ArrayList<>());
        dataMap.put("floats", new ArrayList<>());
        dataMap.put("strings", new ArrayList<>());

        for (String inputFile : inputFiles) {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (INTEGER_PATTERN.matcher(line).matches()) {
                        dataMap.get("integers").add(line);
                    } else if (FLOAT_PATTERN.matcher(line).matches()) {
                        dataMap.get("floats").add(line);
                    } else {
                        dataMap.get("strings").add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Ошибка при чтении файла " + inputFile + ": " + e.getMessage());
            }
        }

        System.out.println("Integers: " + dataMap.get("integers"));
        System.out.println("Floats: " + dataMap.get("floats"));
        System.out.println("Strings: " + dataMap.get("strings"));

        writeDataToFile(dataMap.get("integers"), outputPath, prefix + "integers.txt", appendMode);
        writeDataToFile(dataMap.get("floats"), outputPath, prefix + "floats.txt", appendMode);
        writeDataToFile(dataMap.get("strings"), outputPath, prefix + "strings.txt", appendMode);

        if (shortStats) {
            printShortStats(dataMap);
        }

        if (fullStats) {
            printFullStats(dataMap);
        }
    }

    private static void writeDataToFile(List<String> data, String outputPath, String fileName, boolean appendMode) {
        if (data.isEmpty()) {
            System.out.println("Нет данных для записи " + fileName);
            return;
        }

        Path path = Paths.get(outputPath, fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, appendMode ? StandardOpenOption.APPEND : StandardOpenOption.CREATE)) {
            for (String line : data) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Удалось записать " + fileName);
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл " + path + ": " + e.getMessage());
        }
    }

    private static void printShortStats(Map<String, List<String>> dataMap) {
        System.out.println("Краткая статистика:");
        dataMap.forEach((key, value) -> System.out.println(key + ": " + value.size()));
    }

    private static void printFullStats(Map<String, List<String>> dataMap) {
        System.out.println("Полная статистика:");

        if (!dataMap.get("integers").isEmpty()) {
            List<Integer> integers = dataMap.get("integers").stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList()); // Используем Collectors.toList()
            int min = Collections.min(integers);
            int max = Collections.max(integers);
            int sum = integers.stream().mapToInt(Integer::intValue).sum();
            double avg = integers.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            System.out.println("integers: count=" + integers.size() + ", min=" + min + ", max=" + max + ", sum=" + sum + ", avg=" + avg);
        }

        if (!dataMap.get("floats").isEmpty()) {
            List<Double> floats = dataMap.get("floats").stream()
                    .map(Double::parseDouble)
                    .collect(Collectors.toList()); // Используем Collectors.toList()
            double min = Collections.min(floats);
            double max = Collections.max(floats);
            double sum = floats.stream().mapToDouble(Double::doubleValue).sum();
            double avg = floats.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            System.out.println("floats: count=" + floats.size() + ", min=" + min + ", max=" + max + ", sum=" + sum + ", avg=" + avg);
        }

        if (!dataMap.get("strings").isEmpty()) {
            int minLength = dataMap.get("strings").stream().mapToInt(String::length).min().orElse(0);
            int maxLength = dataMap.get("strings").stream().mapToInt(String::length).max().orElse(0);
            System.out.println("strings: count=" + dataMap.get("strings").size() + ", minLength=" + minLength + ", maxLength=" + maxLength);
        }
    }

    private static void writeTestFile(String fileName, String[] data) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String line : data) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Созданный тестовый файл " + fileName);
        } catch (IOException e) {
            System.err.println("Ошибка при записи тестового файла " + fileName + ": " + e.getMessage());
        }
    }
}
