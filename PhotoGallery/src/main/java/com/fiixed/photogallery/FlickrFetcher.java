package com.fiixed.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by abell on 12/9/13.
 */
public class FlickrFetcher {
    public static final String TAG = "FlickrFetcher";

    private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
    private static final String API_KEY = "f35bbfbdefd8abc3eb01b743fab6d203";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";
    private static final String FORMAT = "json";

    private static final String JSON_PHOTO = "photo";

    private String jsonString;
    private JSONObject picture;



    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<GalleryItem> fetchItems() {
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
        try {
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GET_RECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", FORMAT)
                    .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                    .build().toString();
            Log.i(TAG, url);
            jsonString = getUrl(url);
            jsonString = jsonString.replace("jsonFlickrApi(", "");
            jsonString = jsonString.substring(0,jsonString.lastIndexOf(")"));
            Log.i(TAG, "Received json: " + jsonString);

            parseItems(items);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    void parseItems(ArrayList<GalleryItem> items) {
        try {
            JSONObject rootJSON = new JSONObject(jsonString);
            JSONObject photos = rootJSON.getJSONObject("photos");
            JSONArray photo = photos.getJSONArray("photo");

            for(int i = 0; i < photo.length(); i++) {
                picture = photo.getJSONObject(i);
                String id = picture.getString("id");
                String title = picture.getString("title");
                String smallurl = picture.getString(EXTRA_SMALL_URL);

                GalleryItem item = new GalleryItem();
                item.setId(id);
                item.setTitle(title);
                item.setUrl(smallurl);
                items.add(item);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}

