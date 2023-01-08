package com.github.tvbox.osc.player.thirdparty;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.github.tvbox.osc.base.App;

import java.net.URLEncoder;
import java.util.HashMap;

public class NovaPlayer {
    public static final String TAG = "ThirdParty.NovaPlayer";

    private static final String PACKAGE_NAME = "org.courville.nova";
    private static final String PLAYBACK_ACTIVITY = "org.courville.nova.ActivityScreen";

    private static class NovaPackageInfo {
        final String packageName;
        final String activityName;

        NovaPackageInfo(String packageName, String activityName) {
            this.packageName = packageName;
            this.activityName = activityName;
        }
    }

    private static final NovaPackageInfo[] PACKAGES = {
            new NovaPackageInfo(PACKAGE_NAME, PLAYBACK_ACTIVITY),
    };

    /**
     * @return null if any Nova Player packages not exist.
     */
    public static NovaPackageInfo getPackageInfo() {
        for (NovaPackageInfo pkg : PACKAGES) {
            try {
                ApplicationInfo info = App.getInstance().getPackageManager().getApplicationInfo(pkg.packageName, 0);
                if (info.enabled)
                    return pkg;
                else
                    Log.v(TAG, "Nova Player package `" + pkg.packageName + "` is disabled.");
            } catch (PackageManager.NameNotFoundException ex) {
                Log.v(TAG, "Nova Player package `" + pkg.packageName + "` does not exist.");
            }
        }
        return null;
    }

    private static class Subtitle {
        final Uri uri;
        String name;
        String filename;

        Subtitle(Uri uri) {
            if (uri.getScheme() == null)
                throw new IllegalStateException("Scheme is missed for subtitle URI " + uri);

            this.uri = uri;
        }

        Subtitle(String uriStr) {
            this(Uri.parse(uriStr));
        }
    }


    public static boolean run(Activity activity, String url, String title, String subtitle, HashMap<String, String> headers) {
        NovaPackageInfo packageInfo = getPackageInfo();
        if (packageInfo == null)
            return false;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage(packageInfo.packageName);
            intent.setClassName(packageInfo.packageName, packageInfo.activityName);
            if (headers != null && headers.size() > 0) {
                url = url + "|";
                int idx = 0;
                for (String hk : headers.keySet()) {
                    url += hk + "=" + URLEncoder.encode(headers.get(hk), "UTF-8");
                    if (idx < headers.keySet().size() -1) {
                        url += "&";
                    }
                    idx ++;
                }
            }
            intent.setData(Uri.parse(url));
            intent.putExtra("title", title);

            if (subtitle != null && !subtitle.isEmpty()) {
                Parcelable[] parcels = new Parcelable[1];
                parcels[0] = Uri.parse(subtitle);
                intent.putExtra("subs", parcels);
                intent.putExtra("subs.enable", parcels);
            }
            activity.startActivity(intent);
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "Can't run Nova Player", ex);
            return false;
        }
    }
}