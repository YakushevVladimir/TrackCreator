package ru.phoenix.trackcreator.entity;

import java.util.ArrayList;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class GoogleApiResponse {
    public ArrayList<Result> results;

    public static class Result {
        public double elevation;
        public double resolution;
        public Location location;
    }

    public static class Location {
        public double lat;
        public double lng;
    }
}
