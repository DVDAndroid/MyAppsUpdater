package com.dvd.android.myappsupdater.design;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dvd.android.myappsupdater.R;

public class AppViewHolder extends RecyclerView.ViewHolder {

	protected TextView appName;
	protected TextView packageName;
	protected ImageView appIcon;
	protected CardView card;

	public AppViewHolder(View itemView) {
		super(itemView);

		appName = (TextView) itemView.findViewById(R.id.tv_app_name);
		packageName = (TextView) itemView.findViewById(R.id.tv_app_pack_name);
		appIcon = (ImageView) itemView.findViewById(R.id.iv_app_icon);
		card = (CardView) itemView;

		appName.setSelected(true);
		packageName.setSelected(true);
	}

}
