package com.dvd.android.myappsupdater.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.io.File;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class Utils {

	public static final String LIST_LINK = "https://raw.githubusercontent.com/DVDAndroid/dvdandroid.github.io/master/xml/apps.xml";

	public static final String PREFERENCE_KEY_NAME = "Name";
	public static final String PREFERENCE_KEY_PACKAGE_NAME = "PackageName";
	public static final String PREFERENCE_KEY_DESCRIPTION = "Description";
	public static final String PREFERENCE_KEY_DOWNLOAD_LINK = "DownloadLink";
	public static final String PREFERENCE_KEY_EXTERNAL_LINK = "ExternalLink";
	public static final String PREFERENCE_KEY_GITHUB = "Github";
	public static final String PREFERENCE_KEY_TYPE = "Type";
	public static final String PREFERENCE_KEY_AVAILABLE_LANGS = "AvailableLangs";
	public static final String PREFERENCE_KEY_VERSION_NAME = "VersionName";
	public static final String PREFERENCE_KEY_VERSION_CODE = "VersionCode";
	public static final String PREFERENCE_KEY_MIN_API = "MinApi";
	public static final String PREFERENCE_KEY_TARGET_API = "TargetApi";

	public static final String PREFERENCE_KEY_APP_INFO = "app_info";
	public static final String PREFERENCE_KEY_ANIMATIONS_ENABLED = "animations";
	public static final String PREFERENCE_KEY_CUSTOM_URL = "custom_xml_file";
	public static final String PREFERENCE_KEY_LIBRARIES = "libraries";
	public static final String PREFERENCE_KEY_CONNECTION_TYPE = "connection_type";

	public static CharSequence[] PREFERENCE_KEYS = new CharSequence[] {
			Utils.PREFERENCE_KEY_NAME, Utils.PREFERENCE_KEY_PACKAGE_NAME,
			Utils.PREFERENCE_KEY_DESCRIPTION,
			Utils.PREFERENCE_KEY_DOWNLOAD_LINK,
			Utils.PREFERENCE_KEY_EXTERNAL_LINK, Utils.PREFERENCE_KEY_GITHUB,
			Utils.PREFERENCE_KEY_TYPE, Utils.PREFERENCE_KEY_AVAILABLE_LANGS,
			Utils.PREFERENCE_KEY_VERSION_NAME,
			Utils.PREFERENCE_KEY_VERSION_CODE, Utils.PREFERENCE_KEY_MIN_API,
			Utils.PREFERENCE_KEY_TARGET_API };

	public static boolean isPackageInstalled(Context context, String packagename) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	public static int checkVersion(Context context, String packageName) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(packageName, 0);
			return info.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			return 0;
		}
	}

	public static boolean isConnectedMobile(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	public static boolean hasRoot() {
		return new File("/system/xbin/su").exists()
				|| new File("/system/bin/su").exists();
	}

	public static void createCircularReveal(final View v, boolean fromTop,
			boolean isAnimationEnabled) {
		int cx = v.getWidth() / 2;
		int cy = fromTop ? 0 : v.getHeight() / 2;
		int finalRadius = Math.max(v.getWidth(), v.getHeight());

		SupportAnimator circularReveal = ViewAnimationUtils
				.createCircularReveal(v, cx, cy, 0, finalRadius);
		circularReveal.addListener(new SupportAnimator.AnimatorListener() {
			@Override
			public void onAnimationStart() {
				v.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd() {
			}

			@Override
			public void onAnimationCancel() {
			}

			@Override
			public void onAnimationRepeat() {
			}
		});
		circularReveal.setInterpolator(new AccelerateDecelerateInterpolator());
		circularReveal.setDuration(600);

		if (isAnimationEnabled)
			circularReveal.start();
		else
			v.setVisibility(View.VISIBLE);
	}
}