package com.dvd.android.myappsupdater.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class XMLParser {

	private static final String KEY_APP = "app";
	private static final String KEY_NAME = "name";
	private static final String KEY_PACKAGE_NAME = "packagename";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_DOWNLOAD_LINK = "downloadlink";
	private static final String KEY_DOWNLOAD_CHANNEL = "channel";
	private static final String KEY_EXTERNAL_LINK = "externallink";
	private static final String KEY_GITHUB = "github";
	private static final String KEY_TYPE = "type";
	private static final String KEY_ICON = "icon";
	private static final String KEY_LANGS = "langs";
	private static final String KEY_VERSION_NAME = "versionname";
	private static final String KEY_VERSION_CODE = "versioncode";
	private static final String KEY_MIN_API = "minapi";
	private static final String KEY_TARGET_API = "targetapi";

	public static List<App> readXmlFile(Context ctx) {

		List<App> appsList;
		appsList = new ArrayList<>();

		App curr_app = null;
		String curText = null;

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();

			File file = new File(ctx.getFilesDir() + "/list_app.xml");
			FileInputStream fis = new FileInputStream(file);
			xpp.setInput(new InputStreamReader(fis));

			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tagname = xpp.getName();

				switch (eventType) {
					case XmlPullParser.START_TAG:
						if (tagname.equalsIgnoreCase(KEY_APP))
							curr_app = new App(ctx);
						break;

					case XmlPullParser.TEXT:
						curText = xpp.getText();
						break;

					case XmlPullParser.END_TAG:
						assert curr_app != null;
						switch (tagname.toLowerCase()) {
							case KEY_APP:
								appsList.add(curr_app);
								break;
							case KEY_NAME:
								curr_app.setName(curText);
								break;
							case KEY_PACKAGE_NAME:
								curr_app.setPackageName(curText);
								break;
							case KEY_DESCRIPTION:
								curr_app.setDescription(curText);
								break;
							case KEY_DOWNLOAD_LINK:
								curr_app.setDownloadLink(curText);
								break;
							case KEY_DOWNLOAD_CHANNEL:
								curr_app.setChannel(curText);
								break;
							case KEY_EXTERNAL_LINK:
								curr_app.setExternalLink(curText);
								break;
							case KEY_GITHUB:
								curr_app.setGithub(curText);
								break;
							case KEY_TYPE:
								curr_app.setType(curText);
								break;
							case KEY_ICON:
								curr_app.setAppIcon(curText);
								break;
							case KEY_LANGS:
								curr_app.setAvailableLangs(curText);
								break;
							case KEY_VERSION_NAME:
								curr_app.setVersionName(curText);
								break;
							case KEY_VERSION_CODE:
								curr_app.setVersionCode(Integer
										.parseInt(curText));
								break;
							case KEY_MIN_API:
								curr_app.setMinApi(Integer.parseInt(curText));
								break;
							case KEY_TARGET_API:
								curr_app.setTargetApi(Integer.parseInt(curText));
								break;
						}
						break;
					default:
						break;
				}
				eventType = xpp.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appsList;
	}
}