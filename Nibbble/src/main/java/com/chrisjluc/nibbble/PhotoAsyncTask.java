package com.chrisjluc.nibbble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by chrisjluc on 2/4/2014.
 */
public class PhotoAsyncTask extends AsyncTask<String, String, String> {
    private final static String URL_SHOTS = "http://api.dribbble.com/shots/";
    private final static String IMAGE_URL_KEY = "image_url";
    private final static int RANGE = 5000;

    private PhotoAsyncTaskListener photoAsyncTaskListener;
    private LruCache<String, Bitmap> mMemoryCache;

    private String username;
    private int numberofImages;
    private Context context;
    private boolean isRandom;
    private float displayWidth;
    private float displayHeight;

    /**
     * @param context
     * @param photoAsyncTaskListener
     * @param numberofImages
     * @param isRandom
     * @param username
     * @param mMemoryCache
     */
    public PhotoAsyncTask(Context context, PhotoAsyncTaskListener photoAsyncTaskListener, int numberofImages, boolean isRandom, String username, float displayWidth, float displayHeight, LruCache<String, Bitmap> mMemoryCache) {
        this.context = context;
        this.photoAsyncTaskListener = photoAsyncTaskListener;
        this.numberofImages = numberofImages;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.mMemoryCache = mMemoryCache;
        //TODO: fix
        this.isRandom = true;
        this.username = username;
    }

    @Override
    protected String doInBackground(String... params) {
        String[] imageURLArray = new String[numberofImages];
        if (isRandom) {
            for (int i = 0; i < numberofImages; i++) {
                String imageURL = null;
                while (imageURL == null) {
                    String url = URL_SHOTS + Integer.toString(getRandomNumber());
                    String JSON = getJSONFromURL(url);
                    imageURL = getKeyValue(IMAGE_URL_KEY, JSON);
                }
                imageURLArray[i] = imageURL;
            }
        } else {
            //TODO: when getting followers
        }
        for (int i = 0; i < numberofImages; i++) {
            Bitmap bitmap = downloadImagesFromURL(imageURLArray[i]);
            Bitmap resizedBitmap = resizeBitmap(bitmap);
            int bye = bitmap.getByteCount();
            int newByte = resizedBitmap.getByteCount();
            String fileName = Wallpaper.FILE_NAME + Integer.toString(i) + ".png";
            saveBitmap(resizedBitmap, fileName);
           // addBitmapToMemoryCache(fileName, resizedBitmap);
        }

        return null;
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        int bitmapHeight = bitmap.getHeight();
        int newWidth = Math.round(bitmapHeight * displayWidth / displayHeight);
        return Bitmap.createBitmap(bitmap,0,0,newWidth,bitmapHeight);
    }

    private void saveBitmap(Bitmap bitmap, String fileName) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private Bitmap downloadImagesFromURL(String image_url) {
        Log.i("image_url", image_url);
        InputStream is = null;
        try {
            URL url = new URL(image_url);
            is = url.openStream();
            return BitmapFactory.decodeStream(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Checks for error message, returns null if error
     *
     * @param key
     * @param JSON String for the Json
     * @return the value for the key
     */
    private String getKeyValue(String key, String JSON) {
        try {
            JSONObject jsonObject = new JSONObject(JSON);
            return jsonObject.getString(key);


        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return null;
    }

    /**
     * @param URL
     * @return
     */
    private String getJSONFromURL(String URL) {
        InputStream is = null;

        try {
            int TIMEOUT_MILLISEC = 10000;
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,
                    TIMEOUT_MILLISEC);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
            HttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(URL);
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.i("JSON", json);

        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        return json;
    }

    private int getRandomNumber() {
        return (int) (Math.random() * RANGE) + 1;
    }

    @Override
    protected void onPostExecute(String result) {
        photoAsyncTaskListener.onDownloadComplete();
    }

}
