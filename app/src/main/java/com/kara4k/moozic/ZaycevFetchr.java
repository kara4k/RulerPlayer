package com.kara4k.moozic;


import android.net.Uri;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ZaycevFetchr {

    public static final Uri ENDPOINT = Uri.parse("http://zaycev.net/search.html");

    public static void dodo(String query) throws IOException {

        Document document = Jsoup.connect(createSearchUrl(query)).get();
        Log.e("ZaycevFetchr", "dodo: " + document.toString());
        Elements elementsByClass = document.getElementsByClass("musicset-track clearfix");
        for (int i = 0; i < elementsByClass.size(); i++) {
            Element element = elementsByClass.get(i);
            Elements artist = element.getElementsByClass("musicset-track__track-name");
            Element a = artist.get(0).tagName("a");
            String text = a.text();
            Log.e("ZaycevFetchr", "dodo: " + text);
        }

    }

    private static String createSearchUrl(String query) {
        Uri.Builder builder = ENDPOINT.buildUpon()
                .appendQueryParameter("query_search", query);
        Log.e("ZaycevFetchr", "createSearchUrl: " + builder.build().toString());
        return builder.build().toString();
    }

}
