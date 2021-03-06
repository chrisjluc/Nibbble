package com.chrisjluc.nibbble;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

public class SettingActivity extends Activity {
    public static final String KEY_WIDTH = "key_width";
    public static final String KEY_HEIGHT = "key_height";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingFragment())
                    .commit();
        }

        //Saving display width and height in preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float widthPixels = metrics.widthPixels;
        float heightPixels = metrics.heightPixels;
        editor.putFloat(KEY_WIDTH, widthPixels);
        editor.putFloat(KEY_HEIGHT, heightPixels);
        editor.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(this, NibbleWallpaperService.class));
                NibbleWallpaperService.setHasData(false);
                startActivity(intent);
                NibbleWallpaperService.resumeThread();
                break;
            case R.id.action_stop:
                NibbleWallpaperService.pauseThread();
                break;
            case R.id.action_exit:
                System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
