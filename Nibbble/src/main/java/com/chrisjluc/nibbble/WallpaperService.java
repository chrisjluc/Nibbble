package com.chrisjluc.nibbble;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.IBinder;

import java.util.Timer;

/**
 * Created by chrisjluc on 2/4/2014.
 */
public class WallpaperService extends Service{

    Timer timer;
    //TODO: Make dynamic
    int interval = 5000;
    Drawable drawable;
    WallpaperManager wpm;

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        wpm = WallpaperManager.getInstance(WallpaperService.this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
