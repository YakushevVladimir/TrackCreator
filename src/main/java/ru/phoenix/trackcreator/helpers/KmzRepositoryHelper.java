package ru.phoenix.trackcreator.helpers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.phoenix.trackcreator.entity.Location;
import ru.phoenix.trackcreator.exceptions.TrackCreateException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class KmzRepositoryHelper {

    public static ArrayList<Location> getTrackPoints(File kmzFile) throws TrackCreateException {

        BufferedReader kmlStream = getBufferedReader(kmzFile);
        if (kmlStream == null) {
            throw new TrackCreateException("В указанном kmz-файле маршруты не найдены");
        }

        ArrayList<Location> trackLocations = readLocationsFromKml(kmlStream);
        try {
            kmlStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return trackLocations;
    }

    private static ArrayList<Location> readLocationsFromKml(BufferedReader kmlStream) throws TrackCreateException {
        try {
            ArrayList<Location> result = new ArrayList<Location>();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(kmlStream));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            NodeList coordNode = root.getElementsByTagName("coordinates");
            if (coordNode.getLength() == 0) {
                throw new IllegalArgumentException();
            }

            for (int count = 0; count < coordNode.getLength(); count++) {
                String value = coordNode.item(count).getFirstChild().getNodeValue();
                if (value == null || "".equals(value)) {
                    continue;
                }
                String[] loc = value.trim().split("\\s");
                for (String item : loc) {
                    String[] locValues = item.split(",");
                    if (locValues.length == 3) {
                        double lat = Double.valueOf(locValues[0]);
                        double lon = Double.valueOf(locValues[1]);
                        result.add(new Location(lat, lon));
                    }
                }
            }

            return result;
        } catch (Exception ex) {
            throw new TrackCreateException("Ошибка чтения данных из kml-файла");
        }
    }

    private static BufferedReader getBufferedReader(File kmzFile) throws TrackCreateException {
        try {
            ZipInputStream stream = new ZipInputStream(new FileInputStream(kmzFile));
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().toLowerCase().endsWith("kml")) {
                    continue;
                }
                return new BufferedReader(new InputStreamReader(stream));
            }
            return null;
        } catch (Exception ex) {
            throw new TrackCreateException("Ошибка чтения kmz-файла");
        }
    }
}
