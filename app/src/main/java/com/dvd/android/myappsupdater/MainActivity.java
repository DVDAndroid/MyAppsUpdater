package com.dvd.android.myappsupdater;

import static com.dvd.android.myappsupdater.utils.Utils.LIST_LINK;
import static com.dvd.android.myappsupdater.utils.Utils.PREFERENCE_KEY_ANIMATIONS_ENABLED;
import static com.dvd.android.myappsupdater.utils.Utils.createCircularReveal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import tr.xip.errorview.ErrorView;

import com.dvd.android.myappsupdater.design.CardAdapter;
import com.dvd.android.myappsupdater.design.RecyclerItemClickListener;
import com.dvd.android.myappsupdater.utils.App;
import com.dvd.android.myappsupdater.utils.Utils;
import com.dvd.android.myappsupdater.utils.XMLParser;

public class MainActivity extends AppCompatActivity implements
		RecyclerItemClickListener.OnItemClickListener {

	public static App mClickedApp;
	public static SharedPreferences sharedPreferences;
	public static boolean isAnimationEnabled;
	private List<App> appList;
	private RecyclerView mRecyclerView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private boolean VIA_BROWSER = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_MainActivity);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
		sharedPreferences = getSharedPreferences(getPackageName()
				+ "_preferences", MODE_PRIVATE);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerList);

		linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(
				R.color.green_500));
		mSwipeRefreshLayout
				.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						updateList();
					}
				});
		mRecyclerView.setLayoutManager(linearLayoutManager);
		mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
				this, this));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setExitTransition(new Explode());
			getWindow().setReenterTransition(new Explode());
			getWindow().setSharedElementExitTransition(
					TransitionInflater.from(this).inflateTransition(
							R.transition.transition));
		} else {
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
				SystemBarTintManager tintManager = new SystemBarTintManager(
						this);
				tintManager.setStatusBarTintEnabled(true);
				tintManager.setStatusBarTintColor(getResources().getColor(
						R.color.green_500));
			}
		}

		updateList();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isAnimationEnabled = sharedPreferences.getBoolean(
				PREFERENCE_KEY_ANIMATIONS_ENABLED, true);
	}

	private void updateList() {
		new XMLDownloader(sharedPreferences.getString(
				Utils.PREFERENCE_KEY_CUSTOM_URL, LIST_LINK)).execute();
	}

	@Override
	public void onItemClick(View childView, int position) {
		mClickedApp = appList.get(position);
		Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
				&& isAnimationEnabled) {
			childView.setTransitionName("selectedApp");

			ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
					.makeSceneTransitionAnimation(this, childView,
							childView.getTransitionName());
			startActivity(intent, optionsCompat.toBundle());
		} else
			startActivity(intent);

	}

	@Override
	public void onItemLongPress(View childView, int position) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case R.id.action_settings:
				startActivity(new Intent(Intent.ACTION_MAIN).setClass(this,
						SettingsActivity.class));
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	class XMLDownloader extends AsyncTask<String, Void, Boolean> {

		private MaterialDialog mProgressDialog;
		private URL mUrl;

		public XMLDownloader(String link) {
			try {
				this.mUrl = new URL(link);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (!mSwipeRefreshLayout.isRefreshing()) {
				mProgressDialog = new MaterialDialog.Builder(MainActivity.this)
						.content(R.string.loading).cancelable(false)
						.progress(true, 0).show();
			}
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			try {
				HttpURLConnection c = (HttpURLConnection) mUrl.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();

				FileOutputStream fos = new FileOutputStream(new File(
						MainActivity.this.getFilesDir() + "/list_app.xml"));
				InputStream is = c.getInputStream();

				byte[] buffer = new byte[1024];
				int l;
				while ((l = is.read(buffer)) != -1) {
					fos.write(buffer, 0, l);
				}
				fos.flush();
				fos.getFD().sync();
				fos.close();
				is.close();

				return true;
			} catch (FileNotFoundException | MalformedURLException
					| NullPointerException | ProtocolException
					| SSLHandshakeException | UnknownHostException e) {
				return false;
			} catch (IOException e) {
				return false;
			}

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			ErrorView mLayoutNoInternet = (ErrorView) findViewById(R.id.error_view);
			mLayoutNoInternet.setOnRetryListener(new ErrorView.RetryListener() {
				@Override
				public void onRetry() {
					updateList();
				}
			});

			appList = XMLParser.readXmlFile(MainActivity.this);

			if (result) {
				CardAdapter mAdapter = new CardAdapter(MainActivity.this,
						appList);
				mRecyclerView.setAdapter(mAdapter);

				createCircularReveal(mSwipeRefreshLayout,
						mSwipeRefreshLayout.isRefreshing(), isAnimationEnabled);
				mLayoutNoInternet.setVisibility(View.GONE);
			} else {

				createCircularReveal(mLayoutNoInternet, false,
						isAnimationEnabled);
				mSwipeRefreshLayout.setVisibility(View.GONE);
			}

			if (mSwipeRefreshLayout.isRefreshing())
				mSwipeRefreshLayout.setRefreshing(false);
			else
				mProgressDialog.dismiss();

			if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
				String uri = getIntent().getData().getEncodedQuery();
				if (uri != null) {
					VIA_BROWSER = true;
					String id = uri.replace("id=", "");
					mClickedApp = appList.get(Integer.parseInt(id));
					Intent intent = new Intent(MainActivity.this,
							DetailsActivity.class);
					startActivity(intent);
				}
			}
		}
	}
}