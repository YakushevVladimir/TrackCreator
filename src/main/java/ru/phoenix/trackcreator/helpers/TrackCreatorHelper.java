package ru.phoenix.trackcreator.helpers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.phoenix.trackcreator.entity.Location;
import ru.phoenix.trackcreator.entity.SurveyData;
import ru.phoenix.trackcreator.exceptions.TrackCreateException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class TrackCreatorHelper {

    public static void saveTrack(ArrayList<Location> track, Date trackDate, String dirPath) throws TrackCreateException {
        File filesDir = new File(dirPath);
        if (!filesDir.exists()) {
            throw new TrackCreateException("Указанная папка для сохранения файлов не найдена");
        }
        createTrack(track, trackDate, filesDir);
        createTrackPoints(track, trackDate, filesDir);
    }

    private static void createTrack(ArrayList<Location> points, Date trackDate, File filesDir) throws TrackCreateException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("gpx");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.topografix.com/GPX/1/1");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:gpxx", "http://www.garmin.com/xmlschemas/GpxExtensions/v3");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:gpxtpx", "http://www.garmin.com/xmlschemas/TrackPointExtension/v1");
            rootElement.setAttribute("creator", SurveyData.instance.getTrackDevice());
            rootElement.setAttribute("version", "1.1");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation",
                    "http://www.topografix.com/GPX/1/1 " +
                            "http://www.topografix.com/GPX/1/1/gpx.xsd " +
                            "http://www.garmin.com/xmlschemas/GpxExtensions/v3 " +
                            "http://www8.garmin.com/xmlschemas/GpxExtensionsv3.xsd   " +
                            "http://www.garmin.com/xmlschemas/TrackPointExtension/v1 " +
                            "http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd");
            doc.appendChild(rootElement);

            // metadata elements
            Element metadata = createChild(doc, rootElement, "metadata");

            // metadata -> link elements
            Element link = createChild(doc, metadata, "link");
            link.setAttribute("href", "http://www.garmin.com");

            // metadata -> link -> text element
            createChild(doc, link, "text").setTextContent("Garmin International");

            // metadata -> time elements
            createChild(doc, metadata, "time").setTextContent(TrackHelper.getTrackTime(points.get(points.size() - 1).getTime()));

            Element trk = createChild(doc, rootElement, "trk");

            createChild(doc, trk, "name").setTextContent(getTrackName(trackDate));

            Element extensions = createChild(doc, trk, "extensions");
            Element gpxx_TrackExtension = createChild(doc, extensions, "gpxx:TrackExtension");
            Element gpxx_DisplayColor = createChild(doc, gpxx_TrackExtension, "gpxx:DisplayColor");
            gpxx_DisplayColor.setTextContent("Black");

            Element trkseg = createChild(doc, trk, "trkseg");
            for (Location point : points) {
                Element trkpt = createChild(doc, trkseg, "trkpt");
                trkpt.setAttribute("lat", TrackHelper.getCoordinate(point.getLat()));
                trkpt.setAttribute("lon", TrackHelper.getCoordinate(point.getLon()));

                createChild(doc, trkpt, "ele").setTextContent(TrackHelper.getElevation(point.getEle()));
                createChild(doc, trkpt, "time").setTextContent(TrackHelper.getTrackTime(point.getTime()));

                final int trackCad = SurveyData.instance.getTrackCad();
                if (trackCad > 0) {
                    Element trkpt_extensions = createChild(doc, trkpt, "extensions");
                    Element trackPoint = createChild(doc, trkpt_extensions, "gpxtpx:TrackPointExtension");
                    createChild(doc, trackPoint, "gpxtpx:cad").setTextContent(String.valueOf(trackCad));
                }
            }

            String time = new SimpleDateFormat("dd-MM-yy").format(trackDate);
            saveGpxFile(doc, new File(filesDir, String.format("%s_%s.gpx", SurveyData.instance.getTrackName(), time)));

        } catch (Exception e) {
            e.printStackTrace();
            throw new TrackCreateException(e.getMessage());
        }
    }

    private static String getTrackName(Date trackDate) {
        return String.format("%s_%s", SurveyData.instance.getTrackName(), new SimpleDateFormat("dd-MMM-yy").format(trackDate));
    }

    private static void createTrackPoints(ArrayList<Location> points, Date trackDate, File filesDir) throws TrackCreateException {
        ArrayList<Location> wayPoints = new ArrayList<Location>();
        for (Location point : points) {
            if ("".equals(point.getName())) {
                continue;
            }
            wayPoints.add(point);
        }
        if (wayPoints.size() == 0) {
            return;
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // gpx element

            Element rootElement = doc.createElement("gpx");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.topografix.com/GPX/1/1");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:gpxx", "http://www.garmin.com/xmlschemas/WaypointExtension/v1");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:gpxtrx", "http://www.garmin.com/xmlschemas/GpxExtensions/v3");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:gpxtpx", "http://www.garmin.com/xmlschemas/TrackPointExtension/v1");
            rootElement.setAttribute("creator", SurveyData.instance.getTrackDevice());
            rootElement.setAttribute("version", "1.1");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation",
                    "http://www.topografix.com/GPX/1/1 " +
                            "http://www.topografix.com/GPX/1/1/gpx.xsd " +
                            "http://www.garmin.com/xmlschemas/WaypointExtension/v1 " +
                            "http://www8.garmin.com/xmlschemas/WaypointExtensionv1.xsd " +
                            "http://www.garmin.com/xmlschemas/TrackPointExtension/v1 " +
                            "http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd");
            doc.appendChild(rootElement);

            // metadata element

            Element metadata = createChild(doc, rootElement, "metadata");
            Element link = createChild(doc, metadata, "link");
            link.setAttribute("href", "http://www.garmin.com");
            createChild(doc, link, "text").setTextContent("Garmin International");
            createChild(doc, metadata, "time").setTextContent(TrackHelper.getTrackTime(points.get(points.size() - 1).getTime()));

            // wpt element

            for (Location point : wayPoints) {
                Element wpt = createChild(doc, rootElement, "wpt");
                wpt.setAttribute("lat", TrackHelper.getCoordinate(point.getLat()));
                wpt.setAttribute("lon", TrackHelper.getCoordinate(point.getLon()));

                createChild(doc, wpt, "ele").setTextContent(TrackHelper.getElevation(point.getEle()));
                createChild(doc, wpt, "time").setTextContent(TrackHelper.getTrackTime(point.getTime()));
                createChild(doc, wpt, "name").setTextContent(point.getName());
                createChild(doc, wpt, "sym").setTextContent("Flag, Blue");
            }

            // save file

            String time = new SimpleDateFormat("dd-MM-yy").format(trackDate);
            String wayPointFileName = String.format("Маршр.точки_%s.gpx", time);
            saveGpxFile(doc, new File(filesDir, wayPointFileName));

        } catch (Exception e) {
            e.printStackTrace();
            throw new TrackCreateException(e.getMessage());
        }
    }

    private static void saveGpxFile(Document doc, File wayPointFile) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(wayPointFile);
        transformer.transform(source, result);
    }

    private static Element createChild(Document doc, Element parent, String childName) {
        Element result = doc.createElement(childName);
        parent.appendChild(result);
        return result;
    }
}
