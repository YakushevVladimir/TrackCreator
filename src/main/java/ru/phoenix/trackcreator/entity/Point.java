package ru.phoenix.trackcreator.entity;

import javafx.beans.property.SimpleStringProperty;
import ru.phoenix.trackcreator.helpers.TrackHelper;

import java.util.Date;

/**
* @author Vladimir Yakushev
* @version 1.0
*/
public class Point {
    private final SimpleStringProperty lat;
    private final SimpleStringProperty lng;
    private final SimpleStringProperty ele;
    private final SimpleStringProperty time;
    private final SimpleStringProperty name;

    public Point(double lat, double lng, double ele, Date time, String name) {
        this.lat = new SimpleStringProperty(String.format("%3.10f", lat).replaceAll(",", "."));
        this.lng = new SimpleStringProperty(String.format("%3.10f", lng).replaceAll(",", "."));
        this.ele = new SimpleStringProperty(String.format("%3.2f", ele).replaceAll(",", "."));
        this.time = new SimpleStringProperty(TrackHelper.getTrackTime(time));
        this.name = new SimpleStringProperty(name);
    }

    public Point(Location location) {
        this.lat = new SimpleStringProperty(String.format("%3.10f", location.getLat()).replaceAll(",", "."));
        this.lng = new SimpleStringProperty(String.format("%3.10f", location.getLon()).replaceAll(",", "."));
        this.ele = new SimpleStringProperty(String.format("%3.2f", location.getEle()).replaceAll(",", "."));
        this.time = new SimpleStringProperty(TrackHelper.getTrackTime(location.getTime()));
        this.name = new SimpleStringProperty(location.getName());
    }

    public String getLat() {
        return lat.get();
    }

    public String getLon() {
        return lng.get();
    }

    public String getEle() {
        return ele.get();
    }

    public String getTime() {
        return time.get();
    }

    public String getName() {
        return name.get();
    }

    public void setEle(String ele) {
        this.ele.set(ele);
    }

    public void setLat(String lat) {
        this.lat.set(lat);
    }

    public void setLng(String lng) {
        this.lng.set(lng);
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public void setName(String name) {
        if (name.length() > 50) {
            name = name.substring(0, 50);
        }
        this.name.set(name);
    }

    public SimpleStringProperty eleProperty() {
        return ele;
    }

    public SimpleStringProperty latProperty() {
        return lat;
    }

    public SimpleStringProperty lngProperty() {
        return lng;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty timeProperty() {
        return time;
    }
}
