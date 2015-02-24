package ru.phoenix.trackcreator.entity;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class SurveyData {

    public static SurveyData instance = new SurveyData();
    public static final int DEFAULT_CAD = 0;

    private String trackName;
    private String trackDevice = "Astro 320";
    private Date trackDate1 = new Date();
    private Date trackDate2 = new Date();
    private int trackSpeed = 15;
    private int trackCad = DEFAULT_CAD;

    private ArrayList<Location> baseTrack = new ArrayList<Location>();
    private ArrayList<Location> trackPoints1 = new ArrayList<Location>();
    private ArrayList<Location> trackPoints2 = new ArrayList<Location>();

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public void setTrackDevice(String trackDevice) {
        this.trackDevice = trackDevice;
    }

    public void setTrackSpeed(int trackSpeed) {
        this.trackSpeed = trackSpeed;
    }

    public void setTrackCad(int cad) {
        this.trackCad = cad;
    }

    public void setTrackDate1(Date trackDate) {
        this.trackDate1 = trackDate;
    }
    public void setTrackDate2(Date trackDate) {
        this.trackDate2 = trackDate;
    }


    public String getTrackName() {
        return trackName;
    }

    public String getTrackDevice() {
        return trackDevice;
    }

    public int getTrackSpeed() {
        return trackSpeed;
    }

    public int getTrackCad() {
        return trackCad;
    }

    public Date getTrackDate1() {
        return trackDate1;
    }
    public Date getTrackDate2() {
        return trackDate2;
    }

    public void setBaseTrack(ArrayList<Location> points) {
        baseTrack.clear();
        baseTrack.addAll(points);
    }

    public ArrayList<Location> getBaseTrack() {
        ArrayList<Location> result = new ArrayList<Location>();
        for (Location loc : baseTrack) {
            result.add(loc.clone());
        }
        return result;
    }

    public void setTrackPoints1(ArrayList<Location> points) {
        trackPoints1.clear();
        trackPoints1.addAll(points);
    }

    public ArrayList<Location> getTrackPoints1() {
        return trackPoints1;
    }

    public void setTrackPoints2(ArrayList<Location> points) {
        trackPoints2.clear();
        trackPoints2.addAll(points);
    }

    public ArrayList<Location> getTrackPoints2() {
        return trackPoints2;
    }
}
