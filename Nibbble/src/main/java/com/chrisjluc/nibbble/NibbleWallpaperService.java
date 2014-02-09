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
    private static boolean running = true;
    private static boolean hasData = false;

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {
        private Thread drawWallpaper;
        private boolean isSurfaceReady = false;
        private PhotoAsyncTaskListener downloadListener = new PhotoAsyncTaskListener() {
            @Override
            public void onDownloadComplete() {
                if (!drawWallpaper.isAlive()) {
                    drawWallpaper.start();
                }
                setHasData(true);
            }

            @Override
            public void toastInvalidUsername() {
                Toast.makeText(getApplicationContext(), "Invalid username, please correct username", Toast.LENGTH_LONG).show();
            }

            @Override
            public void toastError() {
                Toast.makeText(getApplicationContext(), "Unexpected error occurred", Toast.LENGTH_LONG).show();
            }
        };

        public WallpaperEngine() {
            if (!hasData) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(NibbleWallpaperService.this);
                int numberofImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
                new PhotoAsyncTask(NibbleWallpaperService.this, downloadListener, numberofImages,
                        sharedPref.getBoolean(SettingFragment.KEY_PREF_PHOTOS_FROM_FOLLOWERS, true),
                        sharedPref.getString(SettingFragment.KEY_PREF_USERNAME, ""),
                        sharedPref.getFloat(SettingActivity.KEY_WIDTH, 0),
                        sharedPref.getFloat(SettingActivity.KEY_HEIGHT, 0)).execute();
                initThread();
            } else {
                if (drawWallpaper == null)
                    initThread();
                if (!drawWallpaper.isAlive()) {
                    drawWallpaper.start();
                }
            }
        }

        private void initThread() {
            drawWallpaper = new Thread(new Runnable() {
                int imageIndex = 0;
                final int waitTime = 1000;
                int timeDisplayed = 0;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(NibbleWallpaperService.this);

                @Override
                public void run() {
                    try {
                        //Draw the inital frame
                        drawFrame(imageIndex);
                        while (true) {
                            if (!running){
                                timeDisplayed = 0;
                            }
                            while (!running) ;

                            int mRepetitionTime = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_REPETITION_TIME, ""));
                            int mNumberOfImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
                            if (timeDisplayed >= mRepetitionTime) {
                                while(!isSurfaceReady);
                                drawFrame(imageIndex);
                                if (++imageIndex >= mNumberOfImages)
                                    imageIndex = 0;
                                timeDisplayed = 0;
                            } else
                                timeDisplayed += waitTime;
                            Thread.sleep(waitTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void drawFrame(int i) {
                SurfaceHolder holder = getSurfaceHolder();
                Canvas canvas = null;
                try {
                    synchronized (holder){
                        canvas = holder.lockCanvas();
                        if (canvas != null)
                            drawImage(canvas, i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }
        }


        private void drawImage(Canvas canvas, int index) {
            Bitmap bitmap;
            FileInputStream fis;
            try {
                fis = NibbleWallpaperService.this.openFileInput(FILE_NAME + index);
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
            isSurfaceReady = true;
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            isSurfaceReady = false;
            super.onSurfaceDestroyed(holder);
        }

    }
        protected static void pauseThread() {
            running = false;
        }

        protected static void resumeThread() {
            running = true;
        }

    protected static void setHasData(boolean hasData1){
        hasData = hasData1;
    }

}
