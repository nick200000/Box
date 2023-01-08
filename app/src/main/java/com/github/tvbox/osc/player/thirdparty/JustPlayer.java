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

public class JustPlayer {
    public static final String TAG = "ThirdParty.Just";

    private static final String PACKAGE_NAME = "com.brouken.player";
    private static final String PLAYBACK_ACTIVITY = "com.brouken.player.MainActivity";

    private static class JustPackageInfo {
        final String packageName;
        final String activityName;

        JustPackageInfo(String packageName, String activityName) {
            this.packageName = packageName;
            this.activityName = activityName;
        }
    }

    private static final JustPackageInfo[] PACKAGES = {
            new JustPackageInfo(PACKAGE_NAME, PLAYBACK_ACTIVITY),
    };

    public static JustPlayerPackageInfo getPackageInfo() {
        for (JustPackageInfo pkg : PACKAGES) {
            try {
                ApplicationInfo info = App.getInstance().getPackageManager().getApplicationInfo(pkg.packageName, 0);
                if (info.enabled)
                    return pkg;
                else
                    Log.v(TAG, "Just Player package `" + pkg.packageName + "` is disabled.");
            } catch (PackageManager.NameNotFoundException ex) {
                Log.v(TAG, "Just Player package `" + pkg.packageName + "` does not exist.");
            }
        }
        return null;
    }

    public static boolean run(Activity activity, String url, String title, String subtitle, HashMap<String, String> headers) {
        JustPackageInfo packageInfo = getPackageInfo();
        if (packageInfo == null)
            return false;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(packageInfo.packageName);
        intent.setComponent(new ComponentName(packageInfo.packageName, packageInfo.activityName));
        intent.setData(Uri.parse(url));
        intent.putExtra("title", title);
        intent.putExtra("name", title);
        intent.putExtra("JustPlayer.extra.title", title);
        if (headers != null && headers.size() > 0) {
            try {
                JSONObject json = new JSONObject();
                for (String key : headers.keySet()) {
                    json.put(key, headers.get(key).trim());
                }
                intent.putExtra("JustPlayer.extra.http_header", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            intent.putExtra("JustPlayer.extra.subtitle", subtitle);
        }
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Can't run JustPlayer", ex);
            return false;
        }
    }
}
