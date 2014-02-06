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
    private Thread drawWallpaper;
    private static boolean running = true;

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {
        private boolean hasData = false;

        private void toggleHasData() {
            if (hasData) {
                hasData = false;
            } else
                hasData = true;
        }

        private PhotoAsyncTaskListener downloadListener = new PhotoAsyncTaskListener() {
            @Override
            public void onDownloadComplete() {
                if (!drawWallpaper.isAlive()) {
                    drawWallpaper.start();
                    toggleHasData();

                }

            }

            @Override
            public void notValidUserName() {
                Toast.makeText(getApplicationContext(), "Invalid username, please correct username", Toast.LENGTH_LONG).show();
            }
        };

        public WallpaperEngine() {
            if (hasData && !drawWallpaper.isAlive()) {
                drawWallpaper.start();
                toggleHasData();
            } else {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(NibbleWallpaperService.this);
                int numberofImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
                new PhotoAsyncTask(NibbleWallpaperService.this, downloadListener, numberofImages,
                        sharedPref.getBoolean(SettingFragment.KEY_PREF_PHOTOS_FROM_FOLLOWERS, true),
                        sharedPref.getString(SettingFragment.KEY_PREF_USERNAME, ""),
                        sharedPref.getFloat(SettingActivity.KEY_WIDTH, 0),
                        sharedPref.getFloat(SettingActivity.KEY_HEIGHT, 0)).execute();
                drawWallpaper = new Thread(new Runnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        try {
                            while (true) {

                                while (!running);

                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(NibbleWallpaperService.this);
                                int tInterval = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_REPETITION, ""));
                                int tNumberofImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
                                if (i >= tNumberofImages)
                                    i = 0;
                                drawFrame(i);
                                i++;
                                Thread.sleep(tInterval);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        private void drawFrame(int i) {
            SurfaceHolder holder = getSurfaceHolder();

            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();

                if (canvas != null) {
                    drawImage(canvas, i);
                } else {


                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void drawImage(Canvas canvas, int i) {
            Bitmap bitmap;
            FileInputStream fis;
            try {
                fis = NibbleWallpaperService.this.openFileInput(FILE_NAME + i + ".png");
                bitmap = BitmapFactory.decodeStream(fis);
                Matrix matrix = new Matrix();
                int wallpaperDesiredMinimumHeight = getWallpaperDesiredMinimumHeight();
                int bitmapHeight = bitmap.getHeight();
                Float scale = (float) wallpaperDesiredMinimumHeight / bitmapHeight;
                matrix.postScale(scale, scale);
                canvas.drawBitmap(bitmap, matrix, null);
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }
    }
    protected static void pauseThread() {
        running = false;
    }

    protected static void resumeThread() {
        running = true;
    }
}
