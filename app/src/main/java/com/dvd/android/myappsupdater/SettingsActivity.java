package com.dvd.android.myappsupdater;

import static com.dvd.android.myappsupdater.MainActivity.sharedPreferences;
import static com.dvd.android.myappsupdater.utils.Utils.LIST_LINK;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_APP_INFO;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_CUSTOM_URL;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_LIBRARIES;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.ref.WeakReference;

public class SettingsActivity extends AppCompatActivity {

	protected static final int DEV_STEPS = 7;
	protected static String DEV_ENABLE = "dev_enable";
	private static Context mContext;

	private static MaterialDialog.Builder createUsedLibrariesAlert() {
		return new MaterialDialog.Builder(mContext)
				.title(R.string.used_libraries).items(R.array.libraries)
				.positiveText(android.R.string.ok)
				.itemsCallback(new MaterialDialog.ListCallback() {
					@Override
					public void onSelection(MaterialDialog materialDialog,
							View view, int i, CharSequence charSequence) {

						String[] links = mContext.getResources()
								.getStringArray(R.array.libraries_link);

						mContext.startActivity(new Intent(Intent.ACTION_VIEW,
								Uri.parse(links[i])));
					}
				});
	}

	private static MaterialDialog.Builder cretaetest() {
		return new MaterialDialog.Builder(mContext)
				.title(R.string.change_xml_file)
				.neutralText(R.string.default_link)
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						super.onPositive(dialog);
						putString(dialog.getInputEditText().getText()
								.toString());
					}

					@Override
					public void onNeutral(MaterialDialog dialog) {
						putString(LIST_LINK);
					}

					protected void putString(String value) {
						sharedPreferences.edit()
								.putString(PREFERENCE_KEY_CUSTOM_URL, value)
								.apply();
					}
				})
				.input(mContext.getString(R.string.insert_link),
						sharedPreferences.getString(PREFERENCE_KEY_CUSTOM_URL,
								LIST_LINK), new MaterialDialog.InputCallback() {
							@Override
							public void onInput(MaterialDialog dialog,
									CharSequence input) {
								boolean isOkText = !input.toString().contains(
										"://")
										|| !input.toString().contains(".xml")
										|| input.toString() == null;

								MDTintHelper.setTint(
										dialog.getInputEditText(),
										mContext.getResources()
												.getColor(
														isOkText ? android.R.color.holo_red_dark
																: android.R.color.holo_green_dark));

								dialog.getActionButton(DialogAction.POSITIVE)
										.setEnabled(!isOkText);
							}
						}).alwaysCallInputCallback();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_SettingsActivity);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setSupportActionBar((Toolbar) findViewById(R.id.settings_toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mContext = this;

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintColor(getResources().getColor(
					R.color.blue_500));
		}

		getFragmentManager().beginTransaction()
				.replace(R.id.content_frame, new SettingsPreference()).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class SettingsPreference extends PreferenceFragment {

		// thanks to multirommgr by tassadar

		private Context mContext;
		private int m_clickCounter;
		private WeakReference<Toast> m_clickCountToast = new WeakReference<>(
				null);

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
			setRetainInstance(true);

			mContext = getActivity().getApplicationContext();
			addDevOptions();

			try {
				PackageInfo pInfo = mContext.getPackageManager()
						.getPackageInfo(mContext.getPackageName(), 0);
				findPreference(PREFERENCE_KEY_APP_INFO).setSummary(
						getString(R.string.version) + "  " + pInfo.versionName
								+ " (" + pInfo.versionCode + ")");
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
				@NonNull Preference preference) {
			switch (preference.getKey().toLowerCase()) {
				case PREFERENCE_KEY_APP_INFO:
					if (m_clickCounter == -1) {
						showDevToast(getString(R.string.already_dev));
						return true;
					}

					++m_clickCounter;

					if (m_clickCounter == DEV_STEPS) {
						sharedPreferences.edit().putBoolean(DEV_ENABLE, true)
								.apply();

						addDevOptions();
						showDevToast(R.string.now_developer);
					} else if (m_clickCounter >= 2) {
						showDevToast(R.string.steps_developer, DEV_STEPS
								- m_clickCounter);
					}
					break;
				case PREFERENCE_KEY_CUSTOM_URL:
					cretaetest().show();
					break;
				case PREFERENCE_KEY_LIBRARIES:
					createUsedLibrariesAlert().show();
					break;
			}

			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}

		private void showDevToast(int stringId, Object... args) {
			showDevToast(getString(stringId, args));
		}

		private void showDevToast(String text) {
			Toast t = m_clickCountToast.get();
			if (t == null) {
				t = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
				m_clickCountToast = new WeakReference<>(t);
			} else {
				t.setText(text);
			}

			t.show();
		}

		private void addDevOptions() {
			if (!sharedPreferences.getBoolean(DEV_ENABLE, false))
				return;

			m_clickCounter = -1;
			addPreferencesFromResource(R.xml.dev_options);
		}

	}
}