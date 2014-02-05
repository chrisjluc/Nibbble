package com.chrisjluc.nibbble;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;

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
    private int interval=1000;
    private Drawable drawable;
    private WallpaperManager wpm;

    private PhotoAsyncTaskListener downloadListener;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadListener = new PhotoAsyncTaskListener() {
            @Override
            public void onDownloadComplete() {
                setWallpapers();
            };
        };
        mytimer=new Timer();
        wpm=WallpaperManager.getInstance(Wallpaper.this);
        new PhotoAsyncTask(this,downloadListener).execute();
    }

    private void setWallpapers(){
        mytimer.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if(i > 5)
                    i = 0;
                Bitmap wallpaper = loadBitmap(FILE_NAME+i);

                try {
                    wpm.setBitmap(wallpaper);

                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
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

    private Bitmap loadBitmap(String fileName){
        Bitmap bitmap = null;
        FileInputStream fis;
        try {
            fis = this.openFileInput(fileName);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
