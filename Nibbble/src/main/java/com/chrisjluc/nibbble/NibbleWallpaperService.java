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
    private boolean hasData = false;

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {
        private Thread drawWallpaper;
        private PhotoAsyncTaskListener downloadListener = new PhotoAsyncTaskListener() {
            @Override
            public void onDownloadComplete() {
                if (!drawWallpaper.isAlive()) {
                    drawWallpaper.start();
                }
                toggleHasData();
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
            if (hasData) {
                toggleHasData();
                if (drawWallpaper == null)
                    initThread();
                if (!drawWallpaper.isAlive()) {
                    drawWallpaper.start();
                }
            } else {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(NibbleWallpaperService.this);
                int numberofImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
                new PhotoAsyncTask(NibbleWallpaperService.this, downloadListener, numberofImages,
                        sharedPref.getBoolean(SettingFragment.KEY_PREF_PHOTOS_FROM_FOLLOWERS, true),
                        sharedPref.getString(SettingFragment.KEY_PREF_USERNAME, ""),
                        sharedPref.getFloat(SettingActivity.KEY_WIDTH, 0),
                        sharedPref.getFloat(SettingActivity.KEY_HEIGHT, 0)).execute();
                initThread();
            }
        }

        private void initThread() {
            drawWallpaper = new Thread(new Runnable() {
                int imageCount = 0;
                final int waitTime = 1000;
                int timeImageDisplay = 0;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(NibbleWallpaperService.this);

                @Override
                public void run() {
                    try {
                        //Draw the inital frame
                        drawFrame(imageCount);
                        while (true) {

                            while (!running) ;
                            int tInterval = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_REPETITION, ""));
                            int tNumberofImages = Integer.parseInt(sharedPref.getString(SettingFragment.KEY_PREF_NUMBER_OF_IMAGES, ""));
                            if (timeImageDisplay >= tInterval) {
                                drawFrame(imageCount);
                                if (++imageCount >= tNumberofImages)
                                    imageCount = 0;
                                timeImageDisplay = 0;
                            } else
                                timeImageDisplay += waitTime;
                            Thread.sleep(waitTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void drawFrame(int i) {
            synchronized (this) {
                SurfaceHolder holder = getSurfaceHolder();

                Canvas canvas = null;

                try {
                    canvas = holder.lockCanvas();

                    if (canvas != null)
                        drawImage(canvas, i);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null)
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

    private void toggleHasData() {
        if (hasData) {
            hasData = false;
        } else
            hasData = true;
    }

}
