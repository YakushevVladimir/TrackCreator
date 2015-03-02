package ru.phoenix.trackcreator.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import ru.phoenix.trackcreator.entity.GoogleApiResponse;
import ru.phoenix.trackcreator.entity.Location;
import ru.phoenix.trackcreator.entity.Point;
import ru.phoenix.trackcreator.entity.SurveyData;
import ru.phoenix.trackcreator.exceptions.TrackCreateException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class TrackHelper {

    public static final String TRACK_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String POINT_NAMES_HASH_PATTERN = "%3.8f,%3.8f";

    private static final int MAX_URI_API_LENGTH = 1900;
    private static final String GOOGLE_API_URL_PATTERN = "http://maps.googleapis.com/maps/api/elevation/json?locations=%s";
    private static final int MAX_API_POINTS = 512;
    private static final int ACCURACY_METERS_FOR_PAUSE = 2;
    private static HashMap<String, Double> elePoints = new HashMap<String, Double>();

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
                    // Есть только данные о координатах
                    trackLocations.add(new Location(lat, lon));
                } else {
                    // Есть данные о координатах и имени точки
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

    public static ArrayList<Location> getTrackWithPause(List<Point> track) {
        ArrayList<Location> result = new ArrayList<Location>();
        int offset = 0;
        int maxIndex = track.size() - 1;
        for (int i = 0; i < track.size(); i++) {
            Point point = track.get(i);
            if (i == 0 || i == maxIndex || point.getOffset() == 0) {
                addPointWithOffset(point.getLocation().clone(), offset, result);
            } else {
                int trackPause = point.getOffset();
                while (trackPause > 0) {
                    Location pausePoint = point.getLocation().clone();
                    pausePoint.setName("");
                    addPointWithOffset(pausePoint, offset, result);
                    int pause = Math.min(getRandomTimeLaps(), trackPause);
                    offset += pause;
                    trackPause -= pause;
                }
                addPointWithOffset(point.getLocation().clone(), offset, result);
            }
        }
        return result;
    }

    private static void addPointWithOffset(Location point, int offset, ArrayList<Location> result) {
        if (offset != 0) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(point.getTime());
            cal.add(Calendar.SECOND, offset);
            point.setTime(cal.getTime());
        }
        result.add(point);
    }

    private static boolean createTrack(ArrayList<Location> trackPoints) throws TrackCreateException {
        if (trackPoints.size() == 0) {
            throw new TrackCreateException("Не удалось получить маршрутные точки из указанного файла!");
        } else if (trackPoints.size() < 2) {
            throw new TrackCreateException("В указанном файле недостаточно маршрутных точек для прокладки маршрута!");
        }

        elePoints.clear();

        // Построение трека

        // дробим маршрут между двумя точками на отрезки с отклонением в заданном радиусе
        trackPoints = splitTrackAtPointsDistance(trackPoints, 500, 75);
        trackPoints = splitTrackAtPointsDistance(trackPoints, 250, 20);
        trackPoints = splitTrackAtPointsDistance(trackPoints, 125, 5);
        trackPoints = splitTrackAtPointsDistance(trackPoints, 10, 5);

        // Сохраняем полученный трек как базовый
        SurveyData.instance.setBaseTrack(trackPoints);

        // Получаем данные о высоте с шагом в 50 метров. Полученную высоту в последующем
        // будем испльзовать для расчета изменения скорости
        ArrayList<Location> points = new ArrayList<Location>();
        int counter = 0;
        final int maxIndex = trackPoints.size() - 1;
        while (counter <= maxIndex) {
            points.add(trackPoints.get(counter));
            if (counter == maxIndex) break;
            counter = Math.min(maxIndex, counter + 5);
        }
        getElevation(points);

        // Создаем маршрут первого дня
        trackPoints =  SurveyData.instance.getBaseTrack();
        trackPoints = splitTrackAtPointsDistance(trackPoints, 1, 1);
        SurveyData.instance.setTrackPoints1(getElevation(
                createFinalTrack(SurveyData.instance.getTrackDate1(), trackPoints)));

        // Создаем маршрут второго дня
        trackPoints =  SurveyData.instance.getBaseTrack();
        trackPoints = splitTrackAtPointsDistance(trackPoints, 1, 1);
        SurveyData.instance.setTrackPoints2(getElevation(
                createFinalTrack(SurveyData.instance.getTrackDate2(), trackPoints)));

        return true;
    }

    private static ArrayList<Location> createFinalTrack(Date startTime, ArrayList<Location> baseTrack) {
        ArrayList<Location> result = new ArrayList<Location>();

        double prevEle = elePoints.size() > 0 ? elePoints.values().iterator().next() : 0;
        double currEle = prevEle;

        // средняя скорость в м/с
        final double speed = SurveyData.instance.getTrackSpeed() * 0.277777778f;

        Calendar cal = new GregorianCalendar();
        cal.setTime(startTime);

        int counter = 0;
        int dist = 0;
        final int maxIndex = baseTrack.size() - 1;
        while (counter <= maxIndex) {
            Location point = baseTrack.get(counter);

            // Читаем высоту текущей точки
            String key = getPointNamesKey(point);
            if (elePoints.containsKey(key)) {
                prevEle = currEle;
                currEle = elePoints.get(key);
            }
            point.setEle(currEle);

            // Добавляем или пропускаем точку в зависимости от условий

            if (counter == 0 || counter == maxIndex) {
                // Добавляем время разгона или остановки для начальной
                // и конечной точек маршрута
                if (counter == 0) addPointWithTime(result, point, cal.getTime());
                addPauseInTrack(result, cal, new Location(point.getLat(), point.getLon(), point.getEle()));
                if (counter == maxIndex) addPointWithTime(result, point, cal.getTime());
                counter++;
                continue;
            } else if (dist > 0 && "".equals(point.getName())) {
                // если точка не именная, то пропускаем её
                dist--;
                counter++;
                continue;
            } else {
                // добавляем точку и может юыть несколько рандомных пауз
                addPointWithRandom(result, cal, point);
            }

            // Вычисляем время и расстояние до следующей точки. В результате получаем число
            // вычленяемых точек


            // время следующей точки
            int timeLaps = getRandomTimeLaps();
            cal.add(Calendar.SECOND, timeLaps);

            // расстояние пройденное за это время с учетом заданной средней скорости, плюс
            // случайное отклонение от скорости в пределах 10%
            dist = (int) (timeLaps * (speed + (speed * Math.random() / 10 * (Math.random() > 0.5 ? 1 : -1))));
            // Увеличение или уменьшение пройденного расстояния в зависимости от изменения ландшафта
            dist = (int) (dist * (prevEle / currEle));

            // так как мы не можем вычленять именнованные точки, то либо пропускаем заданное
            // количество точек в цикле, либо добавляем следующую именную точку
            counter++;
        }

        return result;
    }

    private static void addPointWithRandom(ArrayList<Location> result, Calendar cal, Location point) {
        addPointWithTime(result, point, cal.getTime());
        double percent = Math.random() * 100;
        if (percent > 95) {
            addPauseInTrack(result, cal, new Location(point.getLat(), point.getLon(), point.getEle()));
        }
    }

    private static void addPauseInTrack(ArrayList<Location> result, Calendar cal, Location point) {
        cal.add(Calendar.SECOND, getRandomTimeLaps());
        int numPoints = (int) (Math.random() * 2) + 1;
        for (int i = 0; i < numPoints; i++) {
            addPointWithTime(result, newPausePointWithAccuracy(point), cal.getTime());
            cal.add(Calendar.SECOND, getRandomTimeLaps());
        }
    }

    private static void addPointWithTime(ArrayList<Location> result, Location point, Date currTime) {
        point.setTime(currTime);
        result.add(point);
    }

    private static int getRandomTimeLaps() {
        return (int) (Math.random() * 20) + 5;
    }

    private static ArrayList<Location> splitTrackAtPointsDistance(ArrayList<Location> points, float pointsDist, float accuracyInMeters) {
        if (points.size() < 2) {
            return points;
        }

        ArrayList<Location> result = new ArrayList<Location>();
        result.add(points.get(0).clone());
        for (int i = 1; i < points.size(); i++) {
            Location startPoint = points.get(i - 1);
            Location endPoint = points.get(i);

            int amount = (int) (getDistance(startPoint, endPoint) * 1000 / pointsDist);
            result.addAll(splitTrack(startPoint, endPoint, amount, accuracyInMeters));
            result.add(endPoint.clone());
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
        float ch = amount > 10 ? ((float) amount) / 4 : amount;
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

        if (points.size() == 0) {
            return points;
        }

        // Заполним высотные данные для координат

        int counter = 0;
        StringBuilder builder = new StringBuilder();
        for (Location point : points) {
            String key = getPointNamesKey(point);
            if (elePoints.containsKey(key)) {
                continue;
            }

            if (counter != 0) {
                builder.append("|");
            }
            builder.append(key);

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

        // Установим координаты для точек маршрута

        double lastEle = elePoints.values().iterator().next();
        for (Location point : points) {
            double pointWithEle = elePoints.get(getPointNamesKey(point));
            if (pointWithEle > 0) {
                lastEle = pointWithEle;
            }
            point.setEle(lastEle);
        }

        return points;
    }

    private static Location newPausePointWithAccuracy(Location point) {
        int percent = (int) (Math.random() * 100);
        Location result = addAccuracy(point.clone(), ACCURACY_METERS_FOR_PAUSE, percent > 50);
        result.setName("");
        return result;
    }

    private static Location addAccuracy(Location loc, float meters, boolean inAdd) {
        final double radius = 15 * meters / 1000000;
        double accuracyLat = Math.random() * radius;
        double accuracyLng = Math.random() * radius;

        loc.setLat(loc.getLat() + (accuracyLat * (inAdd ? 1 : -1)));
        loc.setLon(loc.getLon() + (accuracyLng * (inAdd ? 1 : -1)));
        return loc;
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

    private static void getElevationsFromApi(HashMap<String, Double> elePoints, String points) throws TrackCreateException {
        String response = HttpHelper.sendGet(String.format(GOOGLE_API_URL_PATTERN, points));
        try {
            GoogleApiResponse obj = new Gson().fromJson(response, GoogleApiResponse.class);
            for (GoogleApiResponse.Result result : obj.results) {
                String key = getPointNamesKey(result.location.lat, result.location.lng);
                elePoints.put(key, result.elevation);
            }
        } catch (JsonSyntaxException ex) {
            throw new TrackCreateException("Ошибка чтения файла с данными о высоте над уровнем моря для точек маршрута");
        }
    }

    private static String getPointNamesKey(Location location) {
        return getPointNamesKey(location.getLat(), location.getLon());
    }

    private static String getPointNamesKey(double lat, double lon) {
        return String.format(Locale.ENGLISH, POINT_NAMES_HASH_PATTERN, lat, lon);
    }

    public static String getTrackTime(Date time) {
        SimpleDateFormat formater = new SimpleDateFormat(TRACK_TIME_FORMAT);
        formater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formater.format(time);
    }

    public static String getCoordinate(double value) {
        return String.format(Locale.ENGLISH, "%3.10f", value);
    }

    public static String getElevation(double ele) {
        return String.format(Locale.ENGLISH, "%3.2f", ele);
    }
}
