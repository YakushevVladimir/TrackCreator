package ru.phoenix.trackcreator.entity;

import javafx.beans.property.SimpleStringProperty;
import ru.phoenix.trackcreator.helpers.TrackHelper;

import java.util.Date;

/**
* @author Vladimir Yakushev
* @version 1.0
*/
public class Point {
    private final Location location;
    private final SimpleStringProperty lat;
    private final SimpleStringProperty lng;
    private final SimpleStringProperty ele;
    private final SimpleStringProperty time;
    private final SimpleStringProperty name;

    public Point(Location location) {
        this.location = location;
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

    public Location getLocation() {
        return location;
    }

    public void setName(String name) {
        if (name.length() > 50) {
            name = name.substring(0, 50);
        }
        this.name.set(name);
        location.setName(name);
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
