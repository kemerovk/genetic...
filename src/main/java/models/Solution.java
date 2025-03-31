package models;

import java.util.ArrayList;
import java.util.List;

public class Solution implements Comparable<Solution> {

    private final List<City> cities = new ArrayList<City>();

    public Solution(List<City> cities) {
        this.cities.addAll(cities);
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities.clear();
        this.cities.addAll(cities);
    }

    public long distanceBetweenCities(){
        long distance = 0;
        for (int i = 0; i < cities.size() - 1; i++){
            distance += cities.get(i).getDistanceBetweenThisAndSelected(cities.get(i+1).getName());
        }

        return distance;
    }

    public void swapCities(int a, int b){
        City temp = cities.get(a);
        cities.set(a, cities.get(b));
        cities.set(b, temp);
    }

    @Override
    public int compareTo(Solution o) {
        return (int) (this.distanceBetweenCities() - o.distanceBetweenCities());
    }

    public String toString(){
        return cities.toString() + "\nTotal Distance: " + distanceBetweenCities();
    }

    private List<City> getSegment(int firstIndex, int lastIndex) {
        List<City> citiesToReturn = new ArrayList<>();

        for (int i = firstIndex; i <= lastIndex; i++) {
            citiesToReturn.add(cities.get(i));
        }
        return citiesToReturn;
    }

    public void fulfillThisSolution(int first, int last, Solution solution) {
        List<City> citiesToReturn = getSegment(first, last);
        for (int i = 0; i < citiesToReturn.size(); i++) {
            if (i < first || i > last) {
                citiesToReturn.add(i, cities.get(i));
            }
        }
        this.cities.clear();
        this.cities.addAll(citiesToReturn);
    }
}
