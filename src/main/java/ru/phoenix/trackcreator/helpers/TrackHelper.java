package ru.phoenix.trackcreator.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import ru.phoenix.trackcreator.entity.GoogleApiResponse;
import ru.phoenix.trackcreator.entity.Location;
import ru.phoenix.trackcreator.entity.SurveyData;
import ru.phoenix.trackcreator.exceptions.TrackCreateException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class TrackHelper {

    private static final int MAX_URI_API_LENGTH = 1900;
    private static final String GOOGLE_API_URL_PATTERN = "http://maps.googleapis.com/maps/api/elevation/json?locations=%s";
    private static final int MAX_API_POINTS = 512;
    public static final String POINT_NAMES_HASH_PATTERN = "%s_%s";

    public static boolean addLocationFromString(String trackList) throws TrackCreateException {
        String[] locations = trackList.split(";");
        if (locations.length < 2) {
            throw new TrackCreateException("Указано недостаточное количество координат маршрута!");
        }

        ArrayList<Location> trackLocations = new ArrayList<Location>();
        String illegalFormat = "Неверный формат указания координат маршрута!";
        for (String loc : locations) {
            String[] coordinates = loc.split(",");
            if (coordinates.length < 2 || coordinates.length > 3) {
                throw new TrackCreateException(illegalFormat);
            }

            try {
                double lat = Double.valueOf(coordinates[0]);
                double lon = Double.valueOf(coordinates[1]);
                if (coordinates.length == 2) {
                    trackLocations.add(new Location(lat, lon));
                } else {
                    Location location = new Location(lat, lon, coordinates[2]);
                    trackLocations.add(location);
                }
            } catch (Exception ex) {
                throw new TrackCreateException(illegalFormat);
            }
        }

        return createTrack(trackLocations);
    }

    public static boolean addLocationFromFile(String pathname) throws TrackCreateException {
        File kmlFile = new File(pathname);
        if (!kmlFile.exists()) {
            throw new TrackCreateException("Файл с маршрутом не найден!");
        } else if (!kmlFile.getAbsolutePath().toLowerCase().endsWith("kmz")) {
            throw new TrackCreateException("Указан неверный файл с маршрутом!");
        }

        try {
            ArrayList<Location> trackLocations = KmzRepositoryHelper.getTrackPoints(kmlFile);
            if (trackLocations.size() == 0) {
                throw new TrackCreateException("Не удалось получить маршрутные точки из указанного файла!");
            } else if (trackLocations.size() < 2) {
                throw new TrackCreateException("В указанном файле недостаточно маршрутных точек для прокладки маршрута!");
            }
            return createTrack(trackLocations);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || "".equals(errorMessage)) {
                errorMessage = "Ошибка чтения маршрута из kmz-файла!";
            }
            throw new TrackCreateException(errorMessage);
        }
    }

    private static boolean createTrack(ArrayList<Location> trackPoints) throws TrackCreateException {
        if (trackPoints.size() == 0) {
            throw new TrackCreateException("Не удалось получить маршрутные точки из указанного файла!");
        } else if (trackPoints.size() < 2) {
            throw new TrackCreateException("В указанном файле недостаточно маршрутных точек для прокладки маршрута!");
        }

        // Построение трека

        trackPoints = splitTrackAtPointsDistance(trackPoints, 500, 75);
        trackPoints = splitTrackAtPointsDistance(trackPoints, 250, 20);
        trackPoints = splitTrackAtPointsDistance(trackPoints, 125, 5);
        trackPoints = splitTrackAtPointsDistance(trackPoints, 10, 5);

        // Сохраняем полученный трек как базовый

        SurveyData.instance.setBaseTrack(trackPoints);

        // Получаем данные о высоте с шагом в 50 метров

        ArrayList<Location> points = new ArrayList<Location>();
        int counter = 0;
        final int maxIndex = trackPoints.size() - 1;
        while (counter <= maxIndex) {
            points.add(trackPoints.get(counter));
            if (counter == maxIndex) break;
            counter = Math.min(maxIndex, counter + 5);
        }
        ArrayList<Location> elePoints = getElevation(points);

        trackPoints =  SurveyData.instance.getBaseTrack();
        trackPoints = splitTrackAtPointsDistance(trackPoints, 1, 1);
        SurveyData.instance.setTrackPoints1(getElevation(
                createFinalTrack(SurveyData.instance.getTrackDate1(), trackPoints, elePoints)));

        trackPoints =  SurveyData.instance.getBaseTrack();
        trackPoints = splitTrackAtPointsDistance(trackPoints, 1, 1);
        SurveyData.instance.setTrackPoints2(getElevation(
                createFinalTrack(SurveyData.instance.getTrackDate2(), trackPoints, elePoints)));

        return true;
    }

    private static ArrayList<Location> createFinalTrack(Date startTime, ArrayList<Location> track, ArrayList<Location> elePoints) {
        ArrayList<Location> result = new ArrayList<Location>();
        ArrayList<Location> baseTrack = track;

        double prevEle = elePoints.get(0).getEle();
        double currEle = prevEle;

        final double speed = SurveyData.instance.getTrackSpeed() * 0.277777778f;

        Calendar cal = new GregorianCalendar();
        cal.setTime(startTime);

        int counter = 0;
        int dist = 0;
        final int maxIndex = baseTrack.size() - 1;
        while (counter <= maxIndex) {
            Location point = baseTrack.get(counter);

            if (counter == 0 || counter == maxIndex) {
                if (counter == 0) addPointWithTime(result, point, cal.getTime());
                addPauseInTrack(result, cal, new Location(point.getLat(), point.getLon()));
                if (counter == maxIndex) addPointWithTime(result, point, cal.getTime());
                counter++;
                continue;
            } else if (dist > 0 && "".equals(point.getName())) {
                dist--;
                counter++;
                continue;
            }

            if (!"".equals(point.getName())) {
                addNamedPoint(result, point, cal);
            } else {
                addPointWithRandom(result, cal, point);
            }

            int timeLaps = getRandomTimeLaps();
            cal.add(Calendar.SECOND, timeLaps);

            dist = (int) (timeLaps * (speed + (speed * Math.random() / 10 * (Math.random() > 0.5 ? 1 : -1))));
            int index = elePoints.indexOf(point);
            if (index >= 0) {
                prevEle = currEle;
                currEle = elePoints.get(index).getEle();
                dist = (int) (dist * (prevEle / currEle));
            }
            counter++;
        }

        return result;
    }

    private static void addPointWithRandom(ArrayList<Location> result, Calendar cal, Location point) {
        addPointWithTime(result, point, cal.getTime());
        double percent = Math.random() * 100;
        if (percent > 95) {
            addPauseInTrack(result, cal, new Location(point.getLat(), point.getLon()));
        }
    }

    private static void addNamedPoint(ArrayList<Location> result, Location point, Calendar cal) {
        // точка без имени, но в том же месте
        int numPoints = (int) (Math.random() * 2) + 1;
        for (int i = 0; i < numPoints; i++) {
            addPointWithTime(result, new Location(point.getLat(), point.getLon()), cal.getTime());
            cal.add(Calendar.SECOND, getRandomTimeLaps());
        }

        // точка с именем
        addPointWithTime(result, point, cal.getTime());

        // точка без имени, но в том же месте
        cal.add(Calendar.SECOND, getRandomTimeLaps());
        addPointWithTime(result, new Location(point.getLat(), point.getLon()), cal.getTime());
    }

    private static void addPauseInTrack(ArrayList<Location> result, Calendar cal, Location point) {
        cal.add(Calendar.SECOND, getRandomTimeLaps());
        int numPoints = (int) (Math.random() * 2) + 1;
        for (int i = 0; i < numPoints; i++) {
            addPointWithTime(result, new Location(point.getLat(), point.getLon()), cal.getTime());
            cal.add(Calendar.SECOND, getRandomTimeLaps());
        }
    }

    private static void addPointWithTime(ArrayList<Location> result, Location point, Date currTime) {
        point.setTime(currTime);
        result.add(point);
    }

    private static int getRandomTimeLaps() {
        return (int) (Math.random() * 15) + 5;
    }

    private static ArrayList<Location> splitTrackAtPointsDistance(ArrayList<Location> points, float pointsDist, float accuracyInMeters) {
        if (points.size() < 2) {
            return points;
        }

        ArrayList<Location> result = new ArrayList<Location>();
        result.add(points.get(0));
        for (int i = 1; i < points.size(); i++) {
            Location startPoint = points.get(i - 1);
            Location endPoint = points.get(i);

            int amount = (int) (getDistance(startPoint, endPoint) * 1000 / pointsDist);
            result.addAll(splitTrack(startPoint, endPoint, amount, accuracyInMeters));
            result.add(endPoint);
        }

        return result;
    }

    private static ArrayList<Location> splitTrack(Location start, Location end, int amount, float accuracyInMeters) {
        ArrayList<Location> result = new ArrayList<Location>();

        double latStep = (end.getLat() - start.getLat()) / amount;
        double lonStep = (end.getLon() - start.getLon()) / amount;

        double curLat = start.getLat();
        double curLon = start.getLon();
        boolean inAdd = true;
        float ch = ((float) amount) / 4;
        int counter = 0;
        for (int i = 0; i < (amount - 1); i++) {
            curLat += latStep;
            curLon += lonStep;

            if (counter > ch) {
                inAdd = !inAdd;
                counter = 0;
            }
            result.add(addAccuracy(new Location(curLat, curLon), accuracyInMeters, inAdd));
            counter ++;

        }

        return result;
    }

    private static ArrayList<Location> getElevation(ArrayList<Location> points) throws TrackCreateException {
        HashMap<String, Location> elePoints = new HashMap<String, Location>();
        int counter = 0;
        StringBuilder builder = new StringBuilder();
        for (Location point : points) {
            if (counter != 0) {
                builder.append("|");
            }
            builder.append(point.getLat()).append(",").append(point.getLon());

            final String key = getPointNamesKey(point);
            if (!elePoints.containsKey(key)) {
                elePoints.put(key, point);
            }

            counter++;
            if (counter == MAX_API_POINTS || builder.length() > MAX_URI_API_LENGTH) {
                getElevationsFromApi(elePoints, builder.toString());
                counter = 0;
                builder = new StringBuilder();
            }
        }
        if (counter != 0) {
            getElevationsFromApi(elePoints, builder.toString());
        }

        double lastEle = 0;
        for (Location point : points) {
            Location pointWithEle = elePoints.get(getPointNamesKey(point));
            if (pointWithEle != null && pointWithEle.getEle() > 0) {
                lastEle = pointWithEle.getEle();
            }
            point.setEle(lastEle);
        }

        return points;
    }

    private static Location addAccuracy(Location loc, float meters, boolean inAdd) {
        final double radius = 15 * meters / 1000000;
        double accuracyLat = Math.random() * radius;
        double accuracyLng = Math.random() * radius;

        if (inAdd) {
            return new Location(loc.getLat() + accuracyLat, loc.getLon() + accuracyLng);
        } else {
            return new Location(loc.getLat() - accuracyLat, loc.getLon() - accuracyLng);
        }
    }

    private static double getDistance(Location start, Location end) {
        double earthRadius = 6367; //kilometers
        double d2r = Math.PI / 180;
        double dlong = (end.getLon() - start.getLon()) * d2r;
        double dlat = (end.getLat() - start.getLat()) * d2r;
        double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(start.getLat() * d2r)
                        * Math.cos(end.getLat() * d2r) * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return  (float) earthRadius * c;
    }

    private static void getElevationsFromApi(HashMap<String, Location> newLocationPoints, String points) throws TrackCreateException {
        String response = HttpHelper.sendGet(String.format(GOOGLE_API_URL_PATTERN, points));
        try {
            GoogleApiResponse obj = new Gson().fromJson(response, GoogleApiResponse.class);
            for (GoogleApiResponse.Result result : obj.results) {
                String key = getPointNamesKey(result.location.lat, result.location.lng);
                if (newLocationPoints.containsKey(key)) {
                    newLocationPoints.get(key).setEle(result.elevation);
                }
            }
        } catch (JsonSyntaxException ex) {
            throw new TrackCreateException("Ошибка чтения файла с данными о высоте над уровнем моря для точек маршрута");
        }
    }

    private static String getPointNamesKey(Location location) {
        return getPointNamesKey(location.getLat(), location.getLon());
    }

    private static String getPointNamesKey(double lat, double lon) {
        return String.format(POINT_NAMES_HASH_PATTERN, lat, lon);
    }

    public static String getTrackTime(Date time) {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formater.format(time);
    }
}
