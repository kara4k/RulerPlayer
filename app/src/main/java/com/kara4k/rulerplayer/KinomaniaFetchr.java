package com.kara4k.rulerplayer;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class KinomaniaFetchr {

    public static final String SEARCH_ENDPOINT = "http://www.kinomania.ru/search?q=";
    public static final String POPULAR_ENDPOINT = "http://www.kinomania.ru/soundtracks/";

    public static ArrayList<TrackItem> getMovies(String query) throws IOException {
        String searchUrl = createSearchUrl(query);
        Document document = Jsoup.connect(searchUrl).get();
        Elements possible = document.getElementsByClass("section-result-item item2");
        ArrayList<TrackItem> list = new ArrayList<>();

        if (possible.size() == 0) {
            return list;
        }

        addFirstMovie(possible, list);

        Element table = document.getElementsByClass("list-content-item").get(0);
        Elements items = table.getElementsByClass("list-content-item-inner");
        for (int i = 0; i < items.size(); i++) {
            try {
                Elements nameElements = items.get(i).getElementsByClass("name");
                if (nameElements.size() == 0) continue;
                Element nameElement = nameElements.get(0).getElementsByTag("a").get(0);
                String name = nameElement.text();
                String path = nameElement.attr("href");
                String filmYear = items.get(i).getElementsByClass("place").get(0).text();

                TrackItem movieItem = new MovieItem();
                fillMovieItem(movieItem, name, filmYear, path);
                list.add(movieItem);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        return list;
    }

    public static final ArrayList<TrackItem> getPopularMovies() throws IOException {
        Document document = Jsoup.connect(POPULAR_ENDPOINT).get();
        Element table = document.getElementsByClass("bx-mini-slider bx-mini-slider-posters posters--hover").get(0);
        Elements movies = table.getElementsByClass("slide");
        ArrayList list = new ArrayList();
        for (int i = 0; i < movies.size(); i++) {
            Element movie = movies.get(i);
            Element description = movie.getElementsByClass("bx-mini-slider-caption").get(0);
            Element rusTitleLink = description.getElementsByTag("a").get(0);
            String rusName = rusTitleLink.text();
            String path = rusTitleLink.attr("href").replace("soundtracks/", "");
            String engName = description.getElementsByClass("poster-title-eng").get(0).text();
            TrackItem trackItem = new MovieItem();
            fillMovieItem(trackItem, engName, rusName, path);
            list.add(trackItem);
        }
        return list;
    }

    private static void addFirstMovie(Elements possible, ArrayList<TrackItem> list) {
        Element mainLink = possible.get(0).getElementsByTag("a").get(0);
        String firstName = mainLink.text();
        String firstYear = possible.get(0).getElementsByClass("place").text();
        String firstPath = mainLink.attr("href");
        TrackItem firstMovieItem = new MovieItem();
        fillMovieItem(firstMovieItem, firstName, firstYear, firstPath);
        list.add(firstMovieItem);
    }

    private static void fillMovieItem(TrackItem movieItem, String name, String year, String path) {
        movieItem.setName(name);
        movieItem.setTrackName(name);
        movieItem.setTrackArtist(year);
        movieItem.setExtension("");
        String moviePath = String.format("http://www.kinomania.ru%ssoundtracks#filmMenu", path);
        movieItem.setFilePath(moviePath);
        movieItem.setDuration("");
        movieItem.setDurationMs(0);
        movieItem.setTrack(true);
        movieItem.setHasInfo(true);
        movieItem.setOnline(true);
    }

    private static String createSearchUrl(String query) {
        return String.format("%s%s", SEARCH_ENDPOINT, query);
    }

    public static ArrayList<TrackItem> getTracks(String url) throws IOException {
        ArrayList<TrackItem> list = new ArrayList<>();
        Document document = Jsoup.connect(url).get();
        Elements tracksElems = document.getElementsByClass("soundtrack-item clear");
        for (int i = 0; i < tracksElems.size(); i++) {
            String name = tracksElems.get(i).getElementsByClass("soundtrack-item__name-author").get(0).text();
            String artist = tracksElems.get(i).getElementsByClass("soundtrack-item__name-value").get(0).text();
            String duration = tracksElems.get(i).getElementsByClass("soundtrack-item__inner soundtrack-item__time").get(0).text();
            String filePath = tracksElems.get(i).getElementsByTag("audio").get(0).attr("src");

            TrackItem trackItem = new TrackItem();
            trackItem.setName(name);
            trackItem.setTrackName(name);
            trackItem.setTrackArtist(artist);
            trackItem.setFilePath("http://" + filePath.substring(2).replace("//", "/"));

            setDuration(trackItem, duration);
            setTrackDefaults(trackItem);

            list.add(trackItem);
        }
        return list;
    }

    private static void setTrackDefaults(TrackItem trackItem) {
        trackItem.setExtension("MP3");
        trackItem.setTrack(true);
        trackItem.setHasInfo(true);
        trackItem.setOnline(true);
        trackItem.setRadio(false);
    }

    private static void setDuration(TrackItem trackItem, String duration) {
        String[] parts = duration.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int durationMs = minutes * 60 * 1000 + seconds * 1000;
        trackItem.setDurationMs(durationMs);

        String hour = formatDurationPart(hours);
        String min = formatDurationPart(minutes);
        String sec = formatDurationPart(seconds);

        if (hours > 0) {
            trackItem.setDuration(String.format("%s:%s:%s", hour, min, sec));
        } else {
            trackItem.setDuration(String.format("%s:%s", min, sec));
        }
    }

    private static String formatDurationPart(int i) {
        if (i < 10 && i != 0) {
            return "0" + String.valueOf(i);
        } else {
            return String.valueOf(i);
        }
    }


}
