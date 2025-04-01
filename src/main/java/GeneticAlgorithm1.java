import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;

public class GeneticAlgorithm1 {

    private static int[][] matrix;
    private int cityCount;
    private int populationSize;
    private int generations;
    private double mutationRate;
    private int reportInterval;
    private JTextArea outputArea;
    private int stagnationThreshold; // Порог для завершения из-за застоя

    public GeneticAlgorithm1(int cityCount, int populationSize, int generations, double mutationRate, int reportInterval, int stagnationThreshold) {
        this.cityCount = cityCount;
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.reportInterval = reportInterval;
        this.stagnationThreshold = stagnationThreshold; // Инициализируем порог
    }

    public void run() {
        long startTime = System.currentTimeMillis();

        JFrame frame = new JFrame("Genetic Algorithm - TSP");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);

        fillMatrix();

        outputArea.append("Matrix Size: " + cityCount + "x" + cityCount + "\n");

        List<int[]> population = generateInitialPopulation(cityCount);
        int[] bestPath = getBest(population);
        int bestCost = calculateCost(bestPath);

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Обычный генетический алгоритм
        long normalTime = System.currentTimeMillis();
        normalAlgorithm(population, bestPath);
        long normalDuration = System.currentTimeMillis() - normalTime;

        // Параллельный генетический алгоритм
        long parallelTime = System.currentTimeMillis();
        parallelAlgorithm(population, bestPath, executorService);
        long parallelDuration = System.currentTimeMillis() - parallelTime;

        outputArea.append("\nExecution time for normal algorithm: " + normalDuration + "ms\n");
        outputArea.append("Execution time for parallel algorithm: " + parallelDuration + "ms\n");

        executorService.shutdown();

        // Строим графики
        generateChart(normalDuration, parallelDuration);
    }

    private void fillMatrix() {
        matrix = new int[cityCount][cityCount];
        Random rand = new Random();
        for (int i = 0; i < cityCount; i++) {
            for (int j = 0; j <= i; j++) {
                if (i == j) matrix[i][j] = 0;
                else {
                    matrix[i][j] = rand.nextInt(100) + 1;
                    matrix[j][i] = matrix[i][j];
                }
            }
        }
    }

    private List<int[]> generateInitialPopulation(int size) {
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            int[] path = new int[size];
            for (int j = 0; j < size; j++) {
                path[j] = j;
            }
            shuffleArray(path);
            population.add(path);
        }
        return population;
    }

    private void shuffleArray(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }
    }

    private List<int[]> evolve(List<int[]> population) {
        List<int[]> newPopulation = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            int[] parent1 = select(population);
            int[] parent2 = select(population);
            int[] child = orderedCrossover(parent1, parent2);
            if (Math.random() < mutationRate) mutate(child);
            newPopulation.add(child);
        }
        return newPopulation;
    }

    private int[] select(List<int[]> population) {
        population.sort(Comparator.comparingInt(GeneticAlgorithm1::calculateCost));
        return population.get(new Random().nextInt(populationSize / 2));
    }

    private int[] orderedCrossover(int[] parent1, int[] parent2) {
        Random rand = new Random();
        int size = parent1.length;
        int[] child = new int[size];
        Arrays.fill(child, -1);

        int start = rand.nextInt(size);
        int end = rand.nextInt(size - start) + start;

        System.arraycopy(parent1, start, child, start, end - start);

        int index = end % size;
        for (int num : parent2) {
            if (!contains(child, num)) {
                child[index] = num;
                index = (index + 1) % size;
            }
        }

        return child;
    }

    private boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) return true;
        }
        return false;
    }

    private void mutate(int[] path) {
        Random rand = new Random();
        int i = rand.nextInt(path.length);
        int j = rand.nextInt(path.length);
        int temp = path[i];
        path[i] = path[j];
        path[j] = temp;
    }

    private int[] getBest(List<int[]> population) {
        return population.stream().min(Comparator.comparingInt(GeneticAlgorithm1::calculateCost)).orElse(null);
    }

    private static int calculateCost(int[] path) {
        int cost = 0;
        for (int i = 0; i < path.length - 1; i++) {
            cost += matrix[path[i]][path[i + 1]];
        }
        cost += matrix[path[path.length - 1]][path[0]];
        return cost;
    }

    // Реализация обычного алгоритма
    private void normalAlgorithm(List<int[]> population, int[] bestPath) {
        int stagnationCount = 0; // Счетчик стагнации
        int lastBestCost = Integer.MAX_VALUE;

        for (int gen = 0; gen < generations; gen++) {
            population = evolve(population);

            int[] currentBest = getBest(population);
            int currentCost = calculateCost(currentBest);

            if (currentCost < calculateCost(bestPath)) {
                bestPath = currentBest;
                stagnationCount = 0; // Сбросить счетчик стагнации, если решение улучшилось
            } else {
                stagnationCount++;
            }

            if (stagnationCount >= stagnationThreshold) {
                outputArea.append("No improvement after " + stagnationThreshold + " generations. Terminating algorithm.\n");
                break;
            }

            if (gen % reportInterval == 0) {
                outputArea.append("Generation " + gen + " - Best Cost: " + calculateCost(bestPath) + "\n");
            }
        }
    }

    // Реализация параллельного алгоритма
    private void parallelAlgorithm(List<int[]> population, int[] bestPath, ExecutorService executorService) {
        int stagnationCount = 0; // Счетчик стагнации
        int lastBestCost = Integer.MAX_VALUE;

        for (int gen = 0; gen < generations; gen++) {
            population = evolveInParallel(population, executorService);

            int[] currentBest = getBest(population);
            int currentCost = calculateCost(currentBest);

            if (currentCost < calculateCost(bestPath)) {
                bestPath = currentBest;
                stagnationCount = 0; // Сбросить счетчик стагнации, если решение улучшилось
            } else {
                stagnationCount++;
            }

            if (stagnationCount >= stagnationThreshold) {
                outputArea.append("No improvement after " + stagnationThreshold + " generations. Terminating algorithm.\n");
                break;
            }

            if (gen % reportInterval == 0) {
                outputArea.append("Generation " + gen + " - Best Cost: " + calculateCost(bestPath) + "\n");
            }
        }
    }

    private List<int[]> evolveInParallel(List<int[]> population, ExecutorService executorService) {
        List<Callable<List<int[]>>> tasks = new ArrayList<>();

        // Создаем отдельную копию текущего населения для каждого потока
        for (int i = 0; i < populationSize; i++) {
            final List<int[]> populationCopy = new ArrayList<>(population);  // создаем копию для каждого потока
            tasks.add(() -> {
                // Каждый поток работает со своей копией
                int[] parent1 = select(populationCopy);
                int[] parent2 = select(populationCopy);
                int[] child = orderedCrossover(parent1, parent2);
                if (Math.random() < mutationRate) mutate(child);
                return new ArrayList<>(Collections.singletonList(child));
            });
        }

        try {
            // Запуск всех задач параллельно
            List<Future<List<int[]>>> results = executorService.invokeAll(tasks);
            List<int[]> newPopulation = new ArrayList<>();
            for (Future<List<int[]>> result : results) {
                newPopulation.addAll(result.get());
            }
            return newPopulation;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return population;
    }

    // Построение графиков
    private void generateChart(long normalTime, long parallelTime) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(normalTime, "Normal", cityCount + " Cities");
        dataset.addValue(parallelTime, "Parallel", cityCount + " Cities");

        JFreeChart chart = ChartFactory.createBarChart(
                "Comparison of Algorithm Execution Time", // chart title
                "Algorithm",                            // domain axis label
                "Execution Time (ms)",                  // range axis label
                dataset,                                // data
                PlotOrientation.VERTICAL,               // orientation
                true,                                   // include legend
                true,                                   // tooltips
                false                                  // urls
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        JFrame chartFrame = new JFrame("Execution Time Comparison");
        chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chartFrame.add(chartPanel, BorderLayout.CENTER);
        chartFrame.pack();
        chartFrame.setVisible(true);
    }

    public static void main(String[] args) {
        int[][] configurations = {
                {10, 100, 20, 10, 50},
                {50, 200, 100, 20, 50},
                {100, 300, 200, 30, 100},
                {200, 400, 400, 40, 100}
        };

        for (int[] config : configurations) {
            System.out.println("\nRunning Genetic Algorithm for " + config[0] + " cities:");
            GeneticAlgorithm1 ga = new GeneticAlgorithm1(config[0], config[1], config[2], 0.1, config[3], config[4]);
            ga.run();
        }
    }
}
