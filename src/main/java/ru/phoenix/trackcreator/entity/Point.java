package ru.phoenix.trackcreator.entity;

import javafx.beans.property.SimpleStringProperty;
import ru.phoenix.trackcreator.helpers.TrackHelper;

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
    private final SimpleStringProperty offset;

    public Point(Location location) {
        this.location = location;
        this.lat = new SimpleStringProperty(String.format("%3.10f", location.getLat()).replaceAll(",", "."));
        this.lng = new SimpleStringProperty(String.format("%3.10f", location.getLon()).replaceAll(",", "."));
        this.ele = new SimpleStringProperty(String.format("%3.2f", location.getEle()).replaceAll(",", "."));
        this.time = new SimpleStringProperty(TrackHelper.getTrackTime(location.getTime()));
        this.name = new SimpleStringProperty(location.getName());
        this.offset = new SimpleStringProperty("");
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

    public int getOffset() {
        try {
            return Integer.parseInt(offset.get());
        } catch (Exception ex) {
            return 0;
        }
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

    public void setOffset(String offset) {
        try {
            int value = Integer.valueOf(offset.replaceAll("\\D", ""));
            this.offset.set(String.valueOf(value));
        } catch (Exception ex) {
            this.offset.set("");
        }
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

    public SimpleStringProperty offsetProperty() {
        return offset;
    }
}
