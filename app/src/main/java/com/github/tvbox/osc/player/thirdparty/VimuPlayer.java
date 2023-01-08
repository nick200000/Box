package com.github.tvbox.osc.player.thirdparty;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.github.tvbox.osc.base.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class VimuPlayer {
    public static final String TAG = "ThirdParty.VimuPlayer";

    private static final String PACKAGE_NAME = "net.gtvbox.videoplayer";
    private static final String PLAYBACK_ACTIVITY = "net.gtvbox.videoplayer.ActivityScreen";

    private static class VimuPackageInfo {
        final String packageName;
        final String activityName;

        VimuPackageInfo(String packageName, String activityName) {
            this.packageName = packageName;
            this.activityName = activityName;
        }
    }

    private static final VimuPackageInfo[] PACKAGES = {
            new VimuPackageInfo(PACKAGE_NAME, PLAYBACK_ACTIVITY),
    };

    public static VimuPackageInfo getPackageInfo() {
        for (VimuPackageInfo pkg : PACKAGES) {
            try {
                ApplicationInfo info = App.getInstance().getPackageManager().getApplicationInfo(pkg.packageName, 0);
                if (info.enabled)
                    return pkg;
                else
                    Log.v(TAG, "Vimu Player package `" + pkg.packageName + "` is disabled.");
            } catch (PackageManager.NameNotFoundException ex) {
                Log.v(TAG, "Vimu Player package `" + pkg.packageName + "` does not exist.");
            }
        }
        return null;
    }

    public static boolean run(Activity activity, String url, String title, String subtitle, HashMap<String, String> headers) {
        VimuPackageInfo packageInfo = getPackageInfo();
        if (packageInfo == null)
            return false;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(packageInfo.packageName);
        intent.setComponent(new ComponentName(packageInfo.packageName, packageInfo.activityName));
        intent.setData(Uri.parse(url));
        intent.putExtra("title", title);
        intent.putExtra("name", title);
        intent.putExtra("vimu.extra.title", title);
        if (headers != null && headers.size() > 0) {
            try {
                JSONObject json = new JSONObject();
                for (String key : headers.keySet()) {
                    json.put(key, headers.get(key).trim());
                }
                intent.putExtra("vimu.extra.http_header", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            intent.putExtra("vimu.extra.subtitle", subtitle);
        }
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Can't run VimuPlayer", ex);
            return false;
        }
    }
}
