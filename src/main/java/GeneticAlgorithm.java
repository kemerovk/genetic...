import java.util.*;

import models.*;
public class GeneticAlgorithm {
    private static final int POPULATION_SIZE = 50;
    private static final int MAX_GENERATIONS = 1000;
    private static final Random rand = new Random();

    public static void main(String[] args) {
        // 1. Инициализация городов и расстояний между ними
        List<City> cities = initializeCities(20); // 20 городов для примера

        // 2. Создание начальной популяции
        List<Solution> population = initializePopulation(cities, POPULATION_SIZE);

        // 3. Основной цикл генетического алгоритма
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            // Оценка популяции (сортировка по приспособленности)
            Collections.sort(population);

            // Вывод лучшего решения текущего поколения
            if (generation % 100 == 0) {
                System.out.println("Generation " + generation + ": Best distance = " +
                        population.get(0).distanceBetweenCities());
            }

            // Селекция (отбор лучших решений)
            selection(population);

            // Кроссовер (скрещивание решений)
            crossover(population);

            // Мутация (случайные изменения)
            mutation(population);
        }
    }

    private static void mutation(List<Solution> solutions){
        Random rand = new Random();
        int first;
        int second;

        for (Solution solution : solutions) {
            do {
                first = rand.nextInt(solution.getCities().size());
                second = rand.nextInt(solution.getCities().size());
            }
            while (first == second);
            solution.swapCities(first, second);
        }

    }

    private static void selection(List<Solution> solutions){
        Collections.sort(solutions);
        solutions.remove(solutions.get(solutions.size()-1));
    }

    private static void crossover(List<Solution> solutions){
        Random rand = new Random();
        int begin;
        int end;

        for (int i = 0; i < solutions.size() / 2; i++){
            begin = rand.nextInt(solutions.size()/2);
            end = rand.nextInt(solutions.size()/2);
            for (int j = begin; j < end; j++){
                solutions.get(2*i).fulfillThisSolution(begin, end, solutions.get(2*i+1));
                solutions.get(2*i+1).fulfillThisSolution(begin, end, solutions.get(2*i));
            }
        }
    }

    private static List<City> initializeCities(int cityCount) {
        List<City> cities = new ArrayList<>();

        // Создаем города
        for (int i = 0; i < cityCount; i++) {
            cities.add(new City("City" + i));
        }

        // Заполняем расстояния между городами (случайные значения для примера)
        for (int i = 0; i < cityCount; i++) {
            City current = cities.get(i);
            Map<String, Long> distances = new LinkedHashMap<>();

            for (int j = 0; j < cityCount; j++) {
                if (i != j) {
                    distances.put(cities.get(j).getName(), (long) (10 + rand.nextInt(90)));
                }
            }

            current.setDistanceBetweenThisAndOthers(distances);
        }

        return cities;
    }

    private static List<Solution> initializePopulation(List<City> cities, int populationSize) {
        List<Solution> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            // Создаем копию списка городов и перемешиваем его
            List<City> shuffledCities = new ArrayList<>(cities);
            Collections.shuffle(shuffledCities);

            population.add(new Solution(shuffledCities));
        }

        return population;
    }
}