package com.dvd.android.myappsupdater.design;

import static com.dvd.android.myappsupdater.utils.Utils.checkVersion;
import static com.dvd.android.myappsupdater.utils.Utils.isPackageInstalled;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.List;

import com.dvd.android.myappsupdater.R;
import com.dvd.android.myappsupdater.utils.App;

public class CardAdapter extends RecyclerView.Adapter<AppViewHolder> {

	private Context mContext;
	private List<App> mAppsList;

	public CardAdapter(Context context, List<App> apps) {
		this.mContext = context;
		this.mAppsList = new ArrayList<>();
		this.mAppsList.addAll(apps);
	}

	@Override
	public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.card_row, viewGroup, false);

		return new AppViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(AppViewHolder appViewHolder, int i) {
		App app = mAppsList.get(i);
		appViewHolder.appName.setText(app.getName());
		appViewHolder.packageName.setText(app.getPackageName());

		UrlImageViewHelper.setUrlDrawable(appViewHolder.appIcon,
				app.getAppIcon(), R.drawable.ic_launcher_default);

		if (isPackageInstalled(mContext, app.getPackageName())) {
			int versionCode = checkVersion(mContext, app.getPackageName());
			if (app.getVersionCode() > versionCode) {
				appViewHolder.card.setCardBackgroundColor(mContext
						.getResources().getColor(
								android.R.color.holo_green_light));
			}
		}
	}

	@Override
	public int getItemCount() {
		return mAppsList.size();
	}
}
