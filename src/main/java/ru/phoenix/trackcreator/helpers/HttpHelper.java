package ru.phoenix.trackcreator.helpers;

import ru.phoenix.trackcreator.exceptions.TrackCreateException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class HttpHelper {

    private static final String USER_AGENT = "Mozilla/5.0";

    public static String sendGet(String url) throws TrackCreateException {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception ex) {
            throw new TrackCreateException(
                    "Ошибка при к серверу Google" +
                    " для получения данных о высоте над уровнем моря точек маршрута");
        }
    }}
