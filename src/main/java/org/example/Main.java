package org.example;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("Введите путь к файлу или 'exit' для завершения:");

        while (!(input = scanner.nextLine()).equalsIgnoreCase("exit")) {
            File file = new File(input);
            if (!file.exists()) {
                System.out.println("Файл не найден.");
                continue;
            }

            long startTime = System.currentTimeMillis();
            try {
                List<City> cities = file.getName().endsWith(".xml")
                        ? parseXml(file)
                        : parseCsv(file);

                if (cities != null) {
                    generateStatistics(cities);
                }
            } catch (Exception e) {
                System.out.println("Ошибка обработки файла: " + e.getMessage());
            }
            long endTime = System.currentTimeMillis();

            System.out.println("Время обработки: " + (endTime - startTime) + " мс");
            System.out.println("Введите путь к файлу или 'exit' для завершения:");
        }


    }

    private static List<City> parseXml(File file) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(file, xmlMapper.getTypeFactory().constructCollectionType(List.class, City.class));
    }

    private static List<City> parseCsv(File file) throws Exception {
        List<City> cities = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            String[] line;
            reader.readNext(); // Пропустить заголовок
            while ((line = reader.readNext()) != null) {
                cities.add(new City(
                        line[0].replace("\"", ""),
                        line[1].replace("\"", ""),
                        line[2].replace("\"", ""),
                        Integer.parseInt(line[3])
                ));
            }
        }
        return cities;
    }

    private static void generateStatistics(List<City> cities) {
        Map<String, Integer> duplicates = new HashMap<>();
        Map<String, int[]> buildingCounts = new HashMap<>();

        for (City city : cities) {

            String key = city.getCity() + "," + city.getStreet() + "," + city.getHouse()+"," + city.getFloor();
            duplicates.merge(key, 1, Integer::sum);


            buildingCounts.putIfAbsent(city.getCity(), new int[5]);
            if (city.getFloor() >= 1 && city.getFloor() <= 5) {
                buildingCounts.get(city.getCity())[city.getFloor() - 1]++;
            }
        }

        System.out.println("Дублирующиеся записи:");
        duplicates.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() + " раз"));

        System.out.println("Этажность зданий по городам:");
        buildingCounts.forEach((city, counts) -> {
            System.out.print(city + ": ");
            for (int i = 0; i < counts.length; i++) {
                System.out.print((i + 1) + "-этажные = " + counts[i] + " ");
            }
            System.out.println();
        });
    }


    static class City {
        @JacksonXmlProperty(isAttribute = true)
        private String city;

        @JacksonXmlProperty(isAttribute = true)
        private String street;

        @JacksonXmlProperty(isAttribute = true)
        private String house;

        @JacksonXmlProperty(isAttribute = true)
        private int floor;

        public City() {}

        public City(String city, String street, String house, int floor) {
            this.city = city;
            this.street = street;
            this.house = house;
            this.floor = floor;
        }

        public String getCity() {
            return city;
        }

        public String getStreet() {
            return street;
        }

        public String getHouse() {
            return house;
        }

        public int getFloor() {
            return floor;
        }
    }
}
