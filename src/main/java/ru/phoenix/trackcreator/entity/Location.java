package ru.phoenix.trackcreator.entity;

import java.util.Date;

/**
* @author Vladimir Yakushev
* @version 1.0
*/
public class Location {
    private double lat;
    private double lon;
    private double ele;
    private Date time;
    private String name;

    public Location(double lat, double lon) {
        this(lat, lon, "", 0d);
    }

    public Location(double lat, double lon, String name) {
        this(lat, lon, name, 0d);
    }

    public Location(double lat, double lon, String name, double ele) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.ele = ele;
    }

    public Location(double lat, double lon, String name, double ele, Date time) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.ele = ele;
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getEle() {
        return ele;
    }

    public String getName() {
        return name;
    }

    public Date getTime() {
        return time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEle(double ele) {
        this.ele = ele;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public Location clone() {
        return new Location(lat, lon, name, ele, time);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Location) {
            Location loc = (Location) obj;
            return this.lat == loc.getLat() && this.lon == loc.getLon();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Location{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", name='" + name + '\'' +
                '}';
    }
}
