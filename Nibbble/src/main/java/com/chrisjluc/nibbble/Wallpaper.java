package com.chrisjluc.nibbble;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.LruCache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chrisjluc on 2/4/2014.
 */
public class Wallpaper extends Service {
    public final static String FILE_NAME = "image_wallpaper_";
    private Timer mytimer;
    private int initialDelay = 100;
    private int interval;
    private int numberofImages;
    private WallpaperManager wpm;
    protected LruCache<String, Bitmap> mMemoryCache;

    private PhotoAsyncTaskListener downloadListener;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadListener = new PhotoAsyncTaskListener() {
            @Override
            public void onDownloadComplete() {
                setWallpapers();
            }

            ;
        };
        mytimer = new Timer();
        wpm = WallpaperManager.getInstance(Wallpaper.this);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        this.interval = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_REPETITION, ""));
        this.numberofImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
        new PhotoAsyncTask(this, downloadListener, numberofImages,
                !sharedPref.getBoolean(SettingFragment.KEY_PREF_PHOTOS_FROM_FOLLOWERS, true),
                sharedPref.getString(SettingFragment.KEY_PREF_USERNAME, ""),
                sharedPref.getFloat(SettingActivity.KEY_WIDTH, 0),
                sharedPref.getFloat(SettingActivity.KEY_HEIGHT, 0),
                mMemoryCache).execute();
    }

    private void setWallpapers() {

        mytimer.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if (i >= numberofImages)
                    i = 0;
                Bitmap wallpaper = loadBitmap(FILE_NAME + i + ".png");

                try {
                    if (wallpaper != null)
                        wpm.setBitmap(wallpaper);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    i++;
                }

            }
        }, initialDelay, interval);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private Bitmap loadBitmap(String fileName) {
        final Bitmap bitmap = getBitmapFromMemCache(fileName);
        if (bitmap != null) {
            return bitmap;
        } else {
            new AsyncTask<String, Void, Void>() {

                @Override
                protected Void doInBackground(String... params) {
                    Bitmap bitmap = null;
                    FileInputStream fis;
                    try {
                        fis = getApplicationContext().openFileInput(params[0]);
                        bitmap = BitmapFactory.decodeStream(fis);
                        fis.close();
                        addBitmapToMemoryCache(params[0], bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute(fileName);

        }
        return null;
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
