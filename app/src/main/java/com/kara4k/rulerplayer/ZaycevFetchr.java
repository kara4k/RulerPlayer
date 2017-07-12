package com.kara4k.rulerplayer;


import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ZaycevFetchr {

    private static ArrayList<String> stopArray;

    public static final Uri SEARCH_ENDPOINT = Uri.parse("http://zaycev.net/search.html");
    public static final Uri TOP_ENDPOINT = Uri.parse("http://zaycev.net/top/more.html");

    private static List<TrackItem> parseTracks(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        ArrayList<TrackItem> trackItems = new ArrayList<>();
        Elements tracks = document.getElementsByClass("musicset-track clearfix");
        for (int i = 0; i < tracks.size(); i++) {
            TrackItem trackItem = new TrackItem();
            Element element = tracks.get(i);
            int durationMs = Integer.parseInt(element.attr("data-duration")) * 1000;
            String extension = element.attr("data-dkey").split("\\.")[1].toUpperCase();
            String duration = element.getElementsByClass("musicset-track__duration").get(0).text();
            Element artistDiv = element.getElementsByClass("musicset-track__artist").get(0);
            String trackArtist = artistDiv.getElementsByTag("a").text();
            Element nameDiv = element.getElementsByClass("musicset-track__track-name").get(0);
            String trackName = nameDiv.getElementsByTag("a").text();

            String dataUrl = String.format("http://zaycev.net%s", element.attr("data-url"));

            try {
                setTrackUrl(trackItem, dataUrl);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                continue;
            }

            trackItem.setTrackName(trackName);
            trackItem.setTrackArtist(trackArtist);
            trackItem.setDurationMs(durationMs);
            trackItem.setDuration(duration);
            trackItem.setExtension(extension);
            trackItem.setBitrate("");
            trackItem.setOnline(true);
            trackItem.setHasInfo(true);
            trackItem.setTrack(true);
            trackItems.add(trackItem);
        }
        return trackItems;
    }

    public static List<TrackItem> getTracks(String query, int page) throws IOException {
        List<TrackItem> list = parseTracks(createSearchUrl(query, page));

        if (page == 1) {
            if (list.size() < 20) {
                return list;
            } else {
                stopArray = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    stopArray.add(list.get(i).getDuration());
                }
            }
        } else {
            boolean isEqualsFirstPage = true;
            for (int i = 0; i < list.size(); i++) {
                if (!stopArray.get(i).equals(list.get(i).getDuration())) {
                    isEqualsFirstPage = false;
                    break;
                }
            }
            if (isEqualsFirstPage) {
                return null;
            }
        }
        return list;
    }

    public static List<TrackItem> getTracks(int page) throws IOException {
        List<TrackItem> list = parseTracks(createTopUrl(page));
        return list;
    }


//    public static List<TrackItem> searchTracks(String query, int page) throws IOException, JSONException {
//
//        Document document = Jsoup.connect(createSearchUrl(query, page)).get();
//        ArrayList<TrackItem> trackItems = new ArrayList<>();
//        Elements tracks = document.getElementsByClass("musicset-track clearfix");
//        for (int i = 0; i < tracks.size(); i++) {
//            TrackItem trackItem = new TrackItem();
//            Element element = tracks.get(i);
//            int durationMs = Integer.parseInt(element.attr("data-duration")) * 1000;
//            String extension = element.attr("data-dkey").split("\\.")[1].toUpperCase();
//            String duration = element.getElementsByClass("musicset-track__duration").get(0).text();
//            Element artistDiv = element.getElementsByClass("musicset-track__artist").get(0);
//            String trackArtist = artistDiv.getElementsByTag("a").text();
//            Element nameDiv = element.getElementsByClass("musicset-track__track-name").get(0);
//            String trackName = nameDiv.getElementsByTag("a").text();
//
//            String dataUrl = String.format("http://zaycev.net%s", element.attr("data-url"));
//
//            try {
//                setTrackUrl(trackItem, dataUrl);
//            } catch (IOException | JSONException e) {
//                e.printStackTrace();
//                continue;
//            }
//
//            trackItem.setTrackName(trackName);
//            trackItem.setTrackArtist(trackArtist);
//            trackItem.setDurationMs(durationMs);
//            trackItem.setDuration(duration);
//            trackItem.setExtension(extension);
//            trackItem.setBitrate("");
//            trackItem.setOnline(true);
//            trackItem.setHasInfo(true);
//            trackItem.setTrack(true);
//            trackItems.add(trackItem);
//        }
//
//        Log.e("ZaycevFetchr", "searchTracks: " + page);
//
//        if (page == 1) {
//            if (trackItems.size() < 20) {
//                return trackItems;
//            } else {
//                stopArray = new ArrayList<>();
//                for (int i = 0; i < trackItems.size(); i++) {
//                    stopArray.add(trackItems.get(i).getDuration());
//                }
//            }
//        } else {
//            boolean isEqualsFirstPage = true;
//            for (int i = 0; i < trackItems.size(); i++) {
//                if (!stopArray.get(i).equals(trackItems.get(i).getDuration())) {
//                    isEqualsFirstPage = false;
//                    break;
//                }
//            }
//            if (isEqualsFirstPage) {
//                return null;
//            }
//        }
//        return trackItems;
//
//    }


    public static void setTrackUrl(final TrackItem trackItem, final String dataUrl) throws IOException, JSONException {
        try {
            String urlString = getUrlString(dataUrl);
            JSONObject bodyJsonObj = new JSONObject(urlString);
            String filePathUrl = bodyJsonObj.getString("url");
            trackItem.setFilePath(filePathUrl);
        } catch (IOException | JSONException e) {
            trackItem.setFilePath("");
        }
    }

    private static String createSearchUrl(String query, int page) {
        Uri.Builder builder = SEARCH_ENDPOINT.buildUpon()
                .appendQueryParameter("query_search", query)
                .appendQueryParameter("page", String.valueOf(page));
        Log.e("ZaycevFetchr", "createSearchUrl: " + builder.build().toString());
        return builder.build().toString();
    }

    private static String createTopUrl(int page){
        Uri.Builder builder = TOP_ENDPOINT.buildUpon()
                .appendQueryParameter("page", String.valueOf(page));
        Log.e("ZaycevFetchr", "createTopUrl: " + builder.build().toString());
        return builder.build().toString();
    }

    private static byte[] getUrlBites(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }


    static String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBites(urlSpec));
    }


}
