package com.chrisjluc.nibbble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
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
    private final static String URL_FOLLOWING_BEGIN_STRING = "http://api.dribbble.com/players/";
    private final static String URL_FOLLOWING_END_STRING = "/shots/following";
    private final static String KEY_IMAGE_URL = "image_url";
    private final static String KEY_SHOTS = "shots";
    private final static int RANGE = 5000;

    private PhotoAsyncTaskListener photoAsyncTaskListener;
    private String username;
    private int numberofImages;
    private Context context;
    private boolean isError = false;
    private boolean isValidUsername = true;
    private boolean isFollowing;
    private float displayWidth;
    private float displayHeight;

    /**
     * @param context
     * @param photoAsyncTaskListener
     * @param numberofImages
     * @param isFollowing
     * @param username
     */
    public PhotoAsyncTask(Context context, PhotoAsyncTaskListener photoAsyncTaskListener, int numberofImages, boolean isFollowing, String username, float displayWidth, float displayHeight) {
        this.context = context;
        this.photoAsyncTaskListener = photoAsyncTaskListener;
        this.numberofImages = numberofImages;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.isFollowing = isFollowing;
        this.username = username;
    }

    @Override
    protected String doInBackground(String... params) {
        String[] imageURLArray = new String[numberofImages];
        if (isFollowing) {
            String url = URL_FOLLOWING_BEGIN_STRING + username + URL_FOLLOWING_END_STRING;
            String json = getJSONFromURL(url);
            JSONArray shotsJSONArray = null;
            try {
                JSONObject jsonObject = new JSONObject(json);
                shotsJSONArray = jsonObject.getJSONArray(KEY_SHOTS);
            } catch (JSONException e) {
                e.printStackTrace();
                isValidUsername = false;
                return null;
            }
            int length = shotsJSONArray.length();
            try {
                if (length < numberofImages) {
                    for (int i = 0; i < length; i++)
                        imageURLArray[i] = (String) shotsJSONArray.getJSONObject(i).get(KEY_IMAGE_URL);
                    int imageSpotsLeft = numberofImages - length;
                    while (imageSpotsLeft > 0) {
                        imageURLArray[numberofImages - 1 - imageSpotsLeft] = getRandomImageUrl();
                        imageSpotsLeft--;
                    }
                } else {
                    for (int i = 0; i < numberofImages; i++)
                        imageURLArray[i] = (String) shotsJSONArray.getJSONObject(i).get(KEY_IMAGE_URL);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                isError = true;
            }
        } else {
            for (int i = 0; i < numberofImages; i++) {
                imageURLArray[i] = getRandomImageUrl();
            }
        }
        if (isValidUsername) {
            for (int i = 0; i < numberofImages; i++) {
                String fileName = NibbleWallpaperService.FILE_NAME + Integer.toString(i) + ".png";
                Bitmap backupBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
                if (imageURLArray[i] == null) {
                    saveBitmap(backupBitmap, fileName);
                } else {
                    try {
                        Bitmap bitmap = downloadImagesFromURL(imageURLArray[i]);
                        Bitmap resizedBitmap = resizeBitmap(bitmap);
                        saveBitmap(resizedBitmap, fileName);
                    } catch (Exception e) {
                        saveBitmap(backupBitmap, fileName);
                        isError = true;
                    }
                }
            }
        }

        return null;
    }

    private String getRandomImageUrl() {
        String imageURL = null;
        while (imageURL == null) {
            String url = URL_SHOTS + Integer.toString(getRandomNumber());
            String JSON = getJSONFromURL(url);
            imageURL = getKeyValue(KEY_IMAGE_URL, JSON);
        }
        return imageURL;
    }

    /**
     * Center images and crop
     *
     * @param bitmap
     * @return
     */
    private Bitmap resizeBitmap(Bitmap bitmap) throws Exception {
        //Center images and crop
        int centerPixel,centerOffset,bitmapHeight;
        try {
            bitmapHeight = bitmap.getHeight();
            int bitmapWidth = bitmap.getWidth();
            centerPixel = bitmapWidth / 2;
            centerOffset = Math.round(bitmapHeight * displayWidth / displayHeight / 2);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }

        return Bitmap.createBitmap(bitmap, centerPixel - centerOffset, 0, centerPixel + centerOffset, bitmapHeight);
    }

    private void saveBitmap(Bitmap bitmap, String fileName){
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap downloadImagesFromURL(String image_url) throws Exception {
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
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
                isError = true;
            }
        }
        throw new Exception();
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
        if (isError)
            photoAsyncTaskListener.toastError();
        else if(!isValidUsername)
            photoAsyncTaskListener.toastInvalidUsername();
        else
            photoAsyncTaskListener.onDownloadComplete();
    }

}
