package com.chrisjluc.nibbble;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.FileInputStream;

/**
 * Created by chrisjluc on 2/5/2014.
 */
public class NibbleWallpaperService extends WallpaperService {
    public final static String FILE_NAME = "image_wallpaper_";
    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {
        private int interval;
        private int numberofImages;
        private PhotoAsyncTaskListener downloadListener = new PhotoAsyncTaskListener() {
            @Override
            public void onDownloadComplete() {
                drawWallpaper.start();
            }

            @Override
            public void notValidUserName() {
                Toast.makeText(getApplicationContext(), "Invalid username, please correct username", Toast.LENGTH_LONG).show();
            }
        };
        public WallpaperEngine(){

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(NibbleWallpaperService.this);
            this.interval = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_REPETITION, ""));
            this.numberofImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
            new PhotoAsyncTask(NibbleWallpaperService.this, downloadListener, numberofImages,
                    sharedPref.getBoolean(SettingFragment.KEY_PREF_PHOTOS_FROM_FOLLOWERS, true),
                    sharedPref.getString(SettingFragment.KEY_PREF_USERNAME, ""),
                    sharedPref.getFloat(SettingActivity.KEY_WIDTH, 0),
                    sharedPref.getFloat(SettingActivity.KEY_HEIGHT, 0),
                    null).execute();
        }
        Thread drawWallpaper = new Thread(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                try {
                    while (true) {
                        if(i>= numberofImages)
                            i=0;
                        drawFrame(i);
                        i++;
                        Thread.sleep(interval);
                    }
                } catch (Exception e) {
                    //
                }
            }
        });

        private void drawFrame(int i) {
            SurfaceHolder holder = getSurfaceHolder();

            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();

                if (canvas != null) {
                    drawImage(canvas,i);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void drawImage(Canvas canvas,int i) {
            Bitmap bitmap;
            FileInputStream fis;
            try {
                fis = NibbleWallpaperService.this.openFileInput(FILE_NAME + i +".png");
                bitmap = BitmapFactory.decodeStream(fis);
                Matrix matrix = new Matrix();
                int wallpaperDesiredMinimumHeight = getWallpaperDesiredMinimumHeight();
                int bitmapHeight = bitmap.getHeight();
                Float scale = (float) wallpaperDesiredMinimumHeight / bitmapHeight;
                matrix.postScale(scale,scale);
                canvas.drawBitmap(bitmap,matrix,null);
                fis.close();
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}
