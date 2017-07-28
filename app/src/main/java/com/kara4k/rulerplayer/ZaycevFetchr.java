package com.kara4k.rulerplayer;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;

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

public class ZaycevFetchr {

    private static final String UNKNOWN = "---";
    private final Uri SEARCH_ENDPOINT = Uri.parse("http://zaycev.net/search.html");
    private final Uri TOP_ENDPOINT = Uri.parse("http://zaycev.net/top/more.html");

    private Handler mHandler;
    private Context mContext;

    public ZaycevFetchr(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    private void parseTracks(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements tracks = document.getElementsByClass("musicset-track clearfix");

        if (onTracksNotFound(tracks)) return;

        boolean requestSize = Preferences.isRequestSize(mContext);
        for (int i = 0; i < tracks.size(); i++) {
            TrackItem trackItem = new TrackItem();
            Element element = tracks.get(i);
            int durationMs = Integer.parseInt(element.attr("data-duration")) * 1000;
            String duration = element.getElementsByClass("musicset-track__duration").get(0).text();
            Element artistDiv = element.getElementsByClass("musicset-track__artist").get(0);
            String trackArtist = artistDiv.getElementsByTag("a").text();
            Element nameDiv = element.getElementsByClass("musicset-track__track-name").get(0);
            String trackName = nameDiv.getElementsByTag("a").text();

            String dataUrl = String.format("http://zaycev.net%s", element.attr("data-url"));
            setTrackUrl(trackItem, dataUrl, tracks.size());

            if (requestSize) {
                String infoUrl = "http://zaycev.net" + nameDiv.getElementsByTag("a").attr("href");
                getInfo(infoUrl, trackItem, tracks.size());
            } else {
                trackItem.setExtension("MP3");
                trackItem.setBitrate("");
            }

            fillTrackData(trackItem, durationMs, duration, trackArtist, trackName);
        }
    }

    private void fillTrackData(TrackItem trackItem, int durationMs, String duration, String trackArtist, String trackName) {
        trackItem.setTrackName(trackName);
        trackItem.setTrackArtist(trackArtist);
        trackItem.setDurationMs(durationMs);
        trackItem.setDuration(duration);
        trackItem.setOnline(true);
        trackItem.setHasInfo(true);
        trackItem.setTrack(true);
    }

    private boolean onTracksNotFound(Elements tracks) {
        if (tracks.size() == 0) {
            mHandler.obtainMessage(ZaycevTracksParser.ZERO_LENGTH, 0, 0)
                    .sendToTarget();
            return true;
        }
        return false;
    }

    private void getInfo(final String infoUrl, final TrackItem trackItem, final int totalCount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(infoUrl).get();
                    Element details = document.getElementsByClass("audiotrack__details").get(0);
                    String fileSize = details.getElementsByClass("audiotrack__size").text();
                    String bitrate = details.getElementsByClass("audiotrack__bitrate").text();
                    String fSize = fileSize.split(" ")[1];
                    String bRate = bitrate.split(" ")[1];
                    trackItem.setBitrate(bRate);
                    trackItem.setExtension(fSize);
                    sendBitrateReadyMsg();
                } catch (IOException e) {
                    e.printStackTrace();
                    trackItem.setBitrate(UNKNOWN);
                    trackItem.setExtension(UNKNOWN);
                    sendBitrateReadyMsg();
                }
            }

            private void sendBitrateReadyMsg() {
                if (mHandler != null) {
                    mHandler.obtainMessage(
                            ZaycevTracksParser.BITRATE, totalCount, 0, trackItem)
                            .sendToTarget();
                }
            }
        }).start();

    }

    public void getTracks(String query, int page) throws IOException {
        parseTracks(createSearchUrl(query, page));
    }

    public void getTracks(int page) throws IOException {
        parseTracks(createTopUrl(page));
    }


    private void setTrackUrl(final TrackItem trackItem, final String dataUrl, final int totalCount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlString = getUrlString(dataUrl);
                    JSONObject bodyJsonObj = new JSONObject(urlString);
                    String filePathUrl = bodyJsonObj.getString("url");
                    trackItem.setFilePath(filePathUrl);
                    sendPathReadyMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                    trackItem.setFilePath("");
                    sendPathReadyMsg();
                }
            }

            private void sendPathReadyMsg() {
                if (mHandler != null) {
                    mHandler.obtainMessage(
                            ZaycevTracksParser.FILE_PATH, totalCount, 0, trackItem)
                            .sendToTarget();
                }
            }
        }).start();
    }

    private String createSearchUrl(String query, int page) {
        Uri.Builder builder = SEARCH_ENDPOINT.buildUpon()
                .appendQueryParameter("query_search", query)
                .appendQueryParameter("page", String.valueOf(page));
        return builder.build().toString();
    }

    private String createTopUrl(int page) {
        Uri.Builder builder = TOP_ENDPOINT.buildUpon()
                .appendQueryParameter("page", String.valueOf(page));
        return builder.build().toString();
    }

    private byte[] getUrlBites(String urlSpec) throws IOException {
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


    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBites(urlSpec));
    }

    public void clearQueue() {
        mHandler = null;
    }


}
