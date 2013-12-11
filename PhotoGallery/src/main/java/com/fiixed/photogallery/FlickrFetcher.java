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
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";  //defaults to most recent 100 results
    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";  //includes the URL for the small version of the photo if available
    private static final String FORMAT = "json";  //chooses the json response format



    private String jsonString;
    private JSONObject picture;


    /*
    fetches raw data from a URL and returns it as an array of bytes
     */
    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection(); //creates a connection object pointed at the URL

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();  //connects to the endpoint

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0) {  //calls read until the connection runs out of data, InputStream yeilds bytes as they are available
                out.write(buffer, 0, bytesRead);
            }
            out.close();  //once data is finished we close the connection
            return out.toByteArray();  //and return the ByteArrayOutputStream array
        } finally {
            connection.disconnect();
        }
    }

    /*
    converts the result of getURLBytes into a string
     */
    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<GalleryItem> fetchItems() {
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
        try {
            String url = Uri.parse(ENDPOINT).buildUpon()  //Uri.builder convenience class for creating properly escaped parameterized URL's
                    .appendQueryParameter("method", METHOD_GET_RECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", FORMAT)
                    .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                    .build().toString();
            Log.i(TAG, url);
            jsonString = getUrl(url);
            jsonString = jsonString.replace("jsonFlickrApi(", "");
            jsonString = jsonString.substring(0,jsonString.lastIndexOf(")"));  //fixes json format string from Flickr
            Log.i(TAG, "Received json: " + jsonString);

            parseItems(items);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }
    /*
    json parsing and adding GalleryItem objects to ArrayList
     */
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

