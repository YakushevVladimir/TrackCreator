package ru.phoenix.trackcreator.entity;

import java.text.SimpleDateFormat;
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
        this(lat, lon, 0d, "");
    }

    public Location(double lat, double lon, double ele) {
        this(lat, lon, ele, "");
    }

    public Location(double lat, double lon, String name) {
        this(lat, lon, 0d, name);
    }

    public Location(double lat, double lon, double ele, String name) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.ele = ele;
    }

    public Location(double lat, double lon, double ele, Date time, String name) {
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

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
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
        return new Location(lat, lon, ele, time, name);
    }

    @Override
    public String toString() {
        return "Location{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", time=" + new SimpleDateFormat("yy.MM.dd HH:mm:ss").format(time) +
                ", name='" + name + '\'' +
                '}';
    }
}
