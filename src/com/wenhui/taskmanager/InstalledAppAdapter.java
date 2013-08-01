package com.wenhui.taskmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class InstalledAppAdapter extends BaseAdapter {

	private class ViewHolder {
		ImageView icon;
		TextView name;
		TextView location;
		TextView version;
	}

	private List<ApkInfo> apkFiles;
	private LayoutInflater inflater;

	public InstalledAppAdapter(Context context, List<ApkInfo> apkFiles) {

		inflater = LayoutInflater.from(context);
		this.apkFiles = apkFiles;
	}

	@Override
	public int getCount() {
		return apkFiles.size();
	}

	@Override
	public ApkInfo getItem(int position) {
		return apkFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.running_app, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView
					.findViewById(R.id.imageView_app_icon);
			holder.name = (TextView) convertView
					.findViewById(R.id.textView_app_name);
			holder.icon.setAdjustViewBounds(true);
			holder.icon.setScaleType(ScaleType.CENTER_INSIDE);
			holder.location = (TextView) convertView
					.findViewById(R.id.textView_app_info1);
			holder.version = (TextView) convertView
					.findViewById(R.id.textView_app_info2);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ApkInfo item = apkFiles.get(position);
		holder.icon.setImageDrawable(item.getDrawable());
		holder.name.setText(item.getLabel());

		if (item.getSize() == 0) {
			holder.location.setText("On phone");
		} else {
			holder.location.setText("On sdcard");
		}
		holder.version.setText("Version: " + item.getVersion());
		final CheckBox check = (CheckBox)convertView.findViewById(R.id.checkBox_select);
		check.setFocusable(false);
		check.setChecked(apkFiles.get(position).isChecked());
		check.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean checked = !apkFiles.get(position).isChecked();
				check.setChecked(checked);
				apkFiles.get(position).setChecked(checked);
			}
		});
		return convertView;
	}
	
	public ArrayList<String> checkedApkPackageNames(){
		ArrayList<String> packagenames = new ArrayList<String>();
		for(ApkInfo ai: apkFiles){
			if(ai.isChecked()){
				String packageName = ai.getPackageName();
				packagenames.add(packageName);
			}
		}
		return packagenames;
	}

}
