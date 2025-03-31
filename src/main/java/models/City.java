package models;

import java.util.LinkedHashMap;
import java.util.Map;

public class City {
    private String name;
    private final Map<String, Long> distanceBetweenThisAndOthers = new LinkedHashMap<>();

    public City(String name) {
        this.name = name;
    }



    public Map<String, Long> getDistanceBetweenThisAndOthers() {
        return distanceBetweenThisAndOthers;
    }

    public void setDistanceBetweenThisAndOthers(Map<String, Long> distanceBetweenThisAndOthers) {
        this.distanceBetweenThisAndOthers.putAll(distanceBetweenThisAndOthers);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDistanceBetweenThisAndSelected(String city) {
        return distanceBetweenThisAndOthers.get(city);
    }
}
