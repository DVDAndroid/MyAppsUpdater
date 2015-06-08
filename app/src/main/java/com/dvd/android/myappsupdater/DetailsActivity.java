package com.dvd.android.myappsupdater;

import static com.dvd.android.myappsupdater.MainActivity.mClickedApp;
import static com.dvd.android.myappsupdater.MainActivity.sharedPreferences;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_CONNECTION_TYPE;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_DESCRIPTION;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_DOWNLOAD_LINK;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_EXTERNAL_LINK;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_GITHUB;
import static com.dvd.android.myappsupdater.utils.Utils.checkVersion;
import static com.dvd.android.myappsupdater.utils.Utils.hasRoot;
import static com.dvd.android.myappsupdater.utils.Utils.isConnectedMobile;
import static com.dvd.android.myappsupdater.utils.Utils.isPackageInstalled;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mrengineer13.snackbar.SnackBar;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.melnykov.fab.FloatingActionButton;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import com.dvd.android.myappsupdater.utils.App;
import com.dvd.android.myappsupdater.utils.Utils;

public class DetailsActivity extends AppCompatActivity {

	public static MaterialDialog.Builder copy_openAlert;
	private String packageName;
	private FloatingActionButton fab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_DetailsActivity);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		getFragmentManager().beginTransaction()
				.replace(R.id.content_frame, new DetailsSettingsPreference())
				.commit();

		setSupportActionBar((Toolbar) findViewById(R.id.details_toolbar));
		getSupportActionBar().setTitle(mClickedApp.getName());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		packageName = mClickedApp.getPackageName();
		fab = (FloatingActionButton) findViewById(R.id.fab);

		TextView details_tv_app_name = (TextView) findViewById(R.id.details_tv_app_name);
		TextView details_tv_app_pack_name = (TextView) findViewById(R.id.details_tv_app_pack_name);
		ImageView details_iv_app_icon = (ImageView) findViewById(R.id.details_iv_app_icon);

		details_tv_app_name.setText(mClickedApp.getName());
		details_tv_app_pack_name.setText(mClickedApp.getPackageName());
		UrlImageViewHelper.setUrlDrawable(details_iv_app_icon,
				mClickedApp.getAppIcon(), R.drawable.ic_launcher_default);

		details_tv_app_name.setSelected(true);
		details_tv_app_pack_name.setSelected(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setSharedElementEnterTransition(
					TransitionInflater.from(this).inflateTransition(
							R.transition.transition));
		} else {
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
				SystemBarTintManager tintManager = new SystemBarTintManager(
						this);
				tintManager.setStatusBarTintEnabled(true);
				tintManager.setStatusBarTintColor(getResources().getColor(
						R.color.orange_500));
			}
		}

		copy_openAlert = new MaterialDialog.Builder(this)
				.title(R.string.what_now).positiveText(R.string.open)
				.negativeText(android.R.string.copyUrl)
				.neutralText(android.R.string.cancel);

		updateFab();
		animateFab();
	}

	protected boolean downloadYesOrNot() {
		return !isConnectedMobile(this)
				|| sharedPreferences.getBoolean(PREFERENCE_KEY_CONNECTION_TYPE,
						false);
	}

	protected void downloadNow(boolean isAnUpdate) {
		if (!downloadYesOrNot()) {
			new MaterialDialog.Builder(this).title(R.string.warning)
					.titleColorRes(android.R.color.holo_red_dark)
					.content(R.string.only_wifi_warn)
					.positiveText(android.R.string.ok)
					.neutralText(R.string.open_settings)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onNeutral(MaterialDialog dialog) {
							super.onNeutral(dialog);
							startActivity(new Intent(Intent.ACTION_MAIN)
									.setClass(getApplicationContext(),
											SettingsActivity.class));
						}
					}).show();

			return;
		}

		if (mClickedApp.getChannel().equals("STABLE")
				|| mClickedApp.getChannel().equals("RC")
				|| mClickedApp.getChannel().equals(getString(R.string.unknown))) {
			new DownloadApp(mClickedApp).execute();
		} else {
			String message = getString(R.string.warningVersion,
					mClickedApp.getChannel(),
					isAnUpdate ? getString(R.string.update).toLowerCase()
							: getString(R.string.download).toLowerCase());
			new MaterialDialog.Builder(this).title(getString(R.string.warning))
					.titleColorRes(android.R.color.holo_red_dark)
					.content(message).positiveText(android.R.string.ok)
					.negativeText(android.R.string.cancel)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog dialog) {
							super.onPositive(dialog);
							new DownloadApp(mClickedApp).execute();
						}
					}).show();
		}
	}

	/**
	 * Make a subtle animation for a
	 * {@link com.melnykov.fab.FloatingActionButton} drawing attention to the
	 * button.
	 *
	 * @author tvbarthel
	 */
	private void animateFab() {
		fab.postDelayed(new Runnable() {
			@Override
			public void run() {
				// Play a subtle animation
				final long duration = 450;

				final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(
						fab, View.SCALE_X, 1f, 1.2f, 1f);
				scaleXAnimator.setDuration(duration);
				scaleXAnimator.setRepeatCount(2);

				final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(
						fab, View.SCALE_Y, 1f, 1.2f, 1f);
				scaleYAnimator.setDuration(duration);
				scaleYAnimator.setRepeatCount(2);

				scaleXAnimator.start();
				scaleYAnimator.start();

				final AnimatorSet animatorSet = new AnimatorSet();
				animatorSet.play(scaleXAnimator).with(scaleYAnimator);
				animatorSet.start();
			}
		}, 400);
	}

	private void updateFab() {
		boolean isInstalled = isPackageInstalled(this, packageName);
		if (isInstalled) {
			boolean needUpdate = mClickedApp.getVersionCode() > checkVersion(
					this, packageName);
			if (needUpdate) {
				findViewById(R.id.details_tv_update_available).setVisibility(
						View.VISIBLE);

				fab.setImageResource(R.mipmap.ic_autorenew_grey600_24dp);
				fab.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						downloadNow(true);
					}
				});
			} else {
				final Intent i = getPackageManager().getLaunchIntentForPackage(
						packageName);
				if (packageName.equals(getPackageName()) || i == null)
					fab.setVisibility(View.GONE);

				findViewById(R.id.details_tv_update_available).setVisibility(
						View.GONE);
				fab.setImageResource(R.mipmap.ic_play_arrow_grey600_24dp);
				fab.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(i);
					}
				});
			}
		} else {
			findViewById(R.id.details_tv_update_available).setVisibility(
					View.GONE);

			fab.setImageResource(R.mipmap.ic_action_file_download);
			fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					downloadNow(false);
				}
			});
		}
	}

	@Override
	protected void onResume() {
		updateFab();
		invalidateOptionsMenu();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		if (isPackageInstalled(this, packageName)) {
			getMenuInflater().inflate(R.menu.installed_app, menu);

			ActivityManager activityManager = (ActivityManager) this
					.getSystemService(ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager
					.getRunningAppProcesses();
			for (int i = 0; i < procInfos.size(); i++) {
				if (procInfos.get(i).processName.equals(packageName)) {
					menu.findItem(R.id.kill_app).setEnabled(true);
				}
			}
			invalidateOptionsMenu();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.clear_app_data:
				if (hasRoot()) {
					try {
						Runtime.getRuntime().exec(
								"su 0 pm clear " + packageName);
					} catch (IOException e) {
						Toast.makeText(
								this,
								getString(R.string.error_occurred) + "\n"
										+ getString(R.string.root_not_granted),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, getString(R.string.root_required),
							Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.kill_app:
				if (hasRoot()) {
					try {
						Runtime.getRuntime().exec(
								"su 0 am force-stop --user 0 " + packageName);
					} catch (IOException e) {
						Toast.makeText(
								this,
								getString(R.string.error_occurred) + "\n"
										+ getString(R.string.root_not_granted),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, getString(R.string.root_required),
							Toast.LENGTH_SHORT).show();
				}
				invalidateOptionsMenu();
				return true;
			case R.id.uninstall_app:
				Uri packageURI = Uri.parse("package:" + packageName);
				startActivity(new Intent(Intent.ACTION_DELETE, packageURI));
				return true;
			case R.id.manage_app:
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", packageName, null);
				intent.setData(uri);
				startActivity(intent);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public static class DetailsSettingsPreference extends PreferenceFragment {

		long[] mHits = new long[2];

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.app_details);
			setRetainInstance(true);

			for (CharSequence c : Utils.PREFERENCE_KEYS) {
				try {
					Method method = App.class.getMethod("get" + c);
					String value = String.valueOf(method.invoke(mClickedApp));

					findPreference(c).setSummary(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			getPreferenceScreen().removePreference(
					findPreference(PREFERENCE_KEY_DESCRIPTION));
		}

		@Override
		@SuppressWarnings("deprecation")
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
				@NonNull final Preference preference) {

			if (!preference.getSummary().equals(getString(R.string.unknown))) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();
			}

			if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
				if (preference.getKey().equals(PREFERENCE_KEY_GITHUB)
						|| preference.getKey().equals(
								PREFERENCE_KEY_DOWNLOAD_LINK)
						|| preference.getKey().equals(
								PREFERENCE_KEY_EXTERNAL_LINK)) {
					copy_openAlert
							.callback(new MaterialDialog.ButtonCallback() {
								@Override
								public void onPositive(MaterialDialog dialog) {
									Intent browserIntent = new Intent(
											Intent.ACTION_VIEW,
											Uri.parse(String.valueOf(preference
													.getSummary())));
									startActivity(browserIntent);
								}

								@Override
								public void onNegative(MaterialDialog dialog) {
									copy(preference);
								}
							});
					copy_openAlert.show();
				} else {
					copy(preference);
				}
			}

			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}

		@Override
		public void onResume() {
			super.onResume();
		}

		private void copy(final Preference preference) {
			SnackBar.Builder snb = new SnackBar.Builder(getActivity());

			String summary = preference.getSummary().toString();
			summary = summary.length() > 50 ? summary.substring(0,
					Math.min(summary.length(), 75))
					+ "..." : summary;

			snb.withMessage(getString(R.string.clipboard_message) + "\n"
					+ preference.getTitle() + ": " + summary);
			snb.withActionMessageId(android.R.string.ok);
			snb.withStyle(SnackBar.Style.INFO);
			snb.withTextColorId(android.R.color.holo_green_dark);
			snb.show();

			ClipboardManager clipboard = (ClipboardManager) getActivity()
					.getApplicationContext()
					.getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Clipboard",
					preference.getTitle() + ": " + preference.getSummary());
			clipboard.setPrimaryClip(clip);
		}
	}

	class DownloadApp extends AsyncTask<String, Void, Boolean> {

		private MaterialDialog progressDialog;
		private String appName;
		private String downloadLink;

		public DownloadApp(App app) {
			this.appName = app.getName().replaceAll(" ", "_");
			this.downloadLink = app.getDownloadLink();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialog = new MaterialDialog.Builder(DetailsActivity.this)
					.content(R.string.download_apk).cancelable(false)
					.progress(true, 0).negativeText(android.R.string.cancel)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onNegative(MaterialDialog dialog) {
							super.onNegative(dialog);
							cancel(true);
						}
					}).show();
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			try {
				URL url = new URL(downloadLink);
				url.openConnection().connect();

				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/download/" + appName + ".apk");

				byte data[] = new byte[1024];
				int count;
				while ((count = input.read(data)) != -1) {
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();

				return true;
			} catch (FileNotFoundException e) {
				return false;
			} catch (IllegalArgumentException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.dismiss();

			if (result) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/download/" + appName + ".apk")),
						"application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				DetailsActivity.this.startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.error_occurred), Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
}