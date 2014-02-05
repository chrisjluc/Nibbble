package com.chrisjluc.nibbble;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        this.interval = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_REPETITION, ""));
        this.numberofImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
        new PhotoAsyncTask(this, downloadListener, numberofImages, !sharedPref.getBoolean(SettingFragment.KEY_PREF_PHOTOS_FROM_FOLLOWERS, true),sharedPref.getString(SettingFragment.KEY_PREF_USERNAME, "")).execute();
    }

    private void setWallpapers() {
        mytimer.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if (i >= numberofImages)
                    i = 0;
                Bitmap wallpaper = loadBitmap(FILE_NAME + i);

                try {
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
        Bitmap bitmap = null;
        FileInputStream fis;
        try {
            fis = this.openFileInput(fileName);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
