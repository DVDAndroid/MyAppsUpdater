package com.dvd.android.myappsupdater.utils;

import android.content.Context;

import com.dvd.android.myappsupdater.R;

@SuppressWarnings("unused")
public class App {

	private final Context context;
	private String name, appIcon, description, packageName, github,
			downloadLink, channel, externalLink, type, availableLangs,
			versionName;
	private int minApi, targetApi, versionCode;

	public App(Context context) {
		this.context = context;
	}

	public String getName() {
		return name == null ? context.getString(R.string.unknown) : name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAppIcon() {
		return appIcon == null ? context.getString(R.string.unknown) : appIcon;
	}

	public void setAppIcon(String appIcon) {
		this.appIcon = appIcon;
	}

	public String getDescription() {
		return description == null ? context.getString(R.string.unknown)
				: description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPackageName() {
		return packageName == null ? context.getString(R.string.unknown)
				: packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getGithub() {
		return github == null ? context.getString(R.string.unknown) : github;
	}

	public void setGithub(String github) {
		this.github = github;
	}

	public String getDownloadLink() {
		return downloadLink == null ? context.getString(R.string.unknown)
				: downloadLink;
	}

	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}

	public String getChannel() {
		return channel == null ? context.getString(R.string.unknown) : channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getExternalLink() {
		return externalLink == null ? context.getString(R.string.unknown)
				: externalLink;
	}

	public void setExternalLink(String externalLink) {
		this.externalLink = externalLink;
	}

	public String getType() {
		return type == null ? context.getString(R.string.unknown) : type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getMinApi() {
		return minApi == -1 ? 0 : minApi;
	}

	public void setMinApi(int minApi) {
		this.minApi = minApi;
	}

	public int getTargetApi() {
		return targetApi == -1 ? 0 : targetApi;
	}

	public void setTargetApi(int targetApi) {
		this.targetApi = targetApi;
	}

	public String getAvailableLangs() {
		return availableLangs == null ? context.getString(R.string.unknown)
				: availableLangs;
	}

	public void setAvailableLangs(String availableLangs) {
		this.availableLangs = availableLangs;
	}

	public String getVersionName() {
		return versionName == null ? context.getString(R.string.unknown)
				: versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getVersionCode() {
		return versionCode == -1 ? 0 : versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
}
