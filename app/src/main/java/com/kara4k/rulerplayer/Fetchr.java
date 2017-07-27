package com.kara4k.rulerplayer;


import android.net.Uri;
import android.os.Handler;
import android.util.Log;

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

public class Fetchr {

    private Handler mHandler;

    private final Uri SEARCH_ENDPOINT = Uri.parse("http://zaycev.net/search.html");
    private final Uri TOP_ENDPOINT = Uri.parse("http://zaycev.net/top/more.html");

    public Fetchr(Handler handler) {
        mHandler = handler;
    }

    private void parseTracks(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements tracks = document.getElementsByClass("musicset-track clearfix");
        if (tracks.size() == 0) {
            SendObj sendObj = new SendObj();
            sendObj.setTotalCount(0);
            mHandler.obtainMessage(OnlineTracksParser.ZERO_LENGTH, sendObj)
                    .sendToTarget();
            return;
        }

        for (int i = 0; i < tracks.size(); i++) {
            TrackItem trackItem = new TrackItem();
            Element element = tracks.get(i);
            int durationMs = Integer.parseInt(element.attr("data-duration")) * 1000;
//            String extension = element.attr("data-dkey").split("\\.")[1].toUpperCase();
            String duration = element.getElementsByClass("musicset-track__duration").get(0).text();
            Element artistDiv = element.getElementsByClass("musicset-track__artist").get(0);
            String trackArtist = artistDiv.getElementsByTag("a").text();
            Element nameDiv = element.getElementsByClass("musicset-track__track-name").get(0);
            String trackName = nameDiv.getElementsByTag("a").text();

            String infoUrl = "http://zaycev.net" + nameDiv.getElementsByTag("a").attr("href");
            getInfo(infoUrl, trackItem, tracks.size());

            String dataUrl = String.format("http://zaycev.net%s", element.attr("data-url"));
            setTrackUrl(trackItem, dataUrl, tracks.size());

            trackItem.setTrackName(trackName);
            trackItem.setTrackArtist(trackArtist);
            trackItem.setDurationMs(durationMs);
            trackItem.setDuration(duration);
//            trackItem.setExtension(extension);
//            trackItem.setBitrate("");
            trackItem.setOnline(true);
            trackItem.setHasInfo(true);
            trackItem.setTrack(true);
        }
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
                    SendObj sendObj = new SendObj();
                    String fSize = fileSize.split(" ")[1];
                    String bRate = bitrate.split(" ")[1];
                    sendObj.setTotalCount(totalCount);
                    sendObj.setBitrate(bRate);
                    sendObj.setTrackItem(trackItem);
                    sendObj.setFileSize(fSize);
                    if (mHandler != null) {
                        mHandler.obtainMessage(OnlineTracksParser.BITRATE, sendObj).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    SendObj sendObj = new SendObj();
                    sendObj.setTotalCount(totalCount);
                    sendObj.setBitrate("---");
                    sendObj.setTrackItem(trackItem);
                    sendObj.setFileSize("---");
                    if (mHandler != null) {
                        mHandler.obtainMessage(OnlineTracksParser.BITRATE, sendObj).sendToTarget();
                    }
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

//    private void setTrackUrl(final TrackItem trackItem, final String dataUrl) {
//        try {
//            String urlString = getUrlString(dataUrl);
//            JSONObject bodyJsonObj = new JSONObject(urlString);
//            String filePathUrl = bodyJsonObj.getString("url");
//            Log.e("ZaycevFetchr", "setTrackUrl: " + filePathUrl);
//            trackItem.setFilePath(filePathUrl);
//        } catch (IOException | JSONException e) {
//            trackItem.setFilePath("");
//        }
//    }

    private void setTrackUrl(final TrackItem trackItem, final String dataUrl, final int totalCount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("Fetchr", "run: " + "!!!");
                try {
                    String urlString = getUrlString(dataUrl);
                    JSONObject bodyJsonObj = new JSONObject(urlString);
                    String filePathUrl = bodyJsonObj.getString("url");
//                    Log.e("ZaycevFetchr", "setTrackUrl: " + filePathUrl);
//                    trackItem.setFilePath(filePathUrl);
                    SendObj sendObj = new SendObj();
                    sendObj.setTotalCount(totalCount);
                    sendObj.setFilePath(filePathUrl);
                    sendObj.setTrackItem(trackItem);
                    if (mHandler != null) {
                        Log.e("Fetchr", "run: " + "send");
                        mHandler.obtainMessage(OnlineTracksParser.FILE_PATH, sendObj)
                                .sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SendObj sendObj = new SendObj();
                    sendObj.setTotalCount(totalCount);
                    sendObj.setFilePath("");
                    sendObj.setTrackItem(trackItem);
                    if (mHandler != null) {
                        mHandler.obtainMessage(OnlineTracksParser.FILE_PATH, sendObj)
                                .sendToTarget();
                    }
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

    class SendObj {
        private TrackItem mTrackItem;
        private String mBitrate;
        private String mFileSize;
        private int mTotalCount;
        private String mFilePath;

        public TrackItem getTrackItem() {
            return mTrackItem;
        }

        public void setTrackItem(TrackItem trackItem) {
            mTrackItem = trackItem;
        }

        public String getBitrate() {
            return mBitrate;
        }

        public void setBitrate(String bitrate) {
            mBitrate = bitrate;
        }

        public String getFileSize() {
            return mFileSize;
        }

        public void setFileSize(String fileSize) {
            mFileSize = fileSize;
        }

        public int getTotalCount() {
            return mTotalCount;
        }

        public void setTotalCount(int totalCount) {
            mTotalCount = totalCount;
        }

        public String getFilePath() {
            return mFilePath;
        }

        public void setFilePath(String filePath) {
            mFilePath = filePath;
        }
    }
}
