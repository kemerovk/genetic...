import java.sql.SQLOutput;
import java.util.*;

import models.*;
public class GeneticAlgorithm {
    private static final int POPULATION_SIZE = 50;
    private static final int MAX_GENERATIONS = 1000;
    private static final Random rand = new Random();

    public static void main(String[] args) {
        List<City> cities = initializeCities();

        List<Solution> population = initializePopulation(cities);


        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            Collections.sort(population);

            System.out.println("Generation " + generation + ": Best distance = " +
                    population.get(0).distanceBetweenCities());

            System.out.println("b4 sel");
            selection(population);

            System.out.println("b4 cros");
            crossover(population);

            System.out.println("b4 mut");
            mutation(population);

            System.out.println(population.size());
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
            while (first == second || first == 0 || second == 0);
            solution.swapCities(first, second);
        }

    }

    private static void selection(List<Solution> solutions){
        if (solutions.size() >= 2){
            Collections.sort(solutions);
            solutions.remove(solutions.get(solutions.size()-1));
        }
    }

    private static void crossover(List<Solution> solutions) {
        Random rand = new Random();

        int begin;
        int end;
        if (solutions.isEmpty()) return;

        int cityCount = solutions.get(0).getCities().size();

        for (int i = 0; i < solutions.size() / 2; i++) {
            begin = rand.nextInt(cityCount);

            end = rand.nextInt(cityCount - begin) + begin;
            Solution parent1 = solutions.get(2 * i);
            Solution parent2 = solutions.get(2 * i + 1);

            Solution child1 = new Solution(new ArrayList<>(parent1.getCities()));
            Solution child2 = new Solution(new ArrayList<>(parent2.getCities()));

            child1.fulfillThisSolution(begin, end, parent2);
            child2.fulfillThisSolution(begin, end, parent1);

            solutions.set(2 * i, child1);
            solutions.set(2 * i + 1, child2);
        }

    }
    private static List<City> initializeCities() {
        List<City> cities = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            cities.add(new City("City" + i));
        }

        for (int i = 0; i < 20; i++) {
            City current = cities.get(i);
            Map<String, Long> distances = new LinkedHashMap<>();

            for (int j = 0; j < 20; j++) {
                if (i != j) {
                    distances.put(cities.get(j).getName(), (long) (10 + rand.nextInt(90)));
                }
            }

            current.setDistanceBetweenThisAndOthers(distances);
        }

        return cities;
    }

    private static List<Solution> initializePopulation(List<City> cities) {
        List<Solution> population = new ArrayList<>();

        for (int i = 0; i < GeneticAlgorithm.POPULATION_SIZE; i++) {
            List<City> shuffledCities = new ArrayList<>(cities);
            Collections.shuffle(shuffledCities);

            population.add(new Solution(shuffledCities));
        }

        return population;
    }
}