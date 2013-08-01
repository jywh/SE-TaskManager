package com.wenhui.taskmanager;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter {

	private class ViewHolder {
		ImageView icon;
		TextView name;
		TextView size;
		TextView version;
	}

	private List<ApkInfo> apkFiles;
	private LayoutInflater inflater;

	public FileAdapter(Context context, List<ApkInfo> apkFiles) {

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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.install_app, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView
					.findViewById(R.id.imageView_icon);
			holder.name = (TextView) convertView
					.findViewById(R.id.textView_filename);
			holder.icon.setAdjustViewBounds(true);
			holder.icon.setScaleType(ScaleType.CENTER_INSIDE);
			holder.size = (TextView) convertView
					.findViewById(R.id.textView_size);
			holder.version = (TextView) convertView
					.findViewById(R.id.textView_version);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ApkInfo item = apkFiles.get(position);
		holder.icon.setImageDrawable(item.getDrawable());
		holder.name.setText(item.getLabel());
		String lenght = toReadFormatSize(item.getSize());
		holder.size.setText(lenght);
		holder.version.setText("Version: " + item.getVersion());
		return convertView;
	}

	private final long KB = 1024;
	private final long MB = KB * 1024;
	private final long GB = MB * 1024;

	private String toReadFormatSize(long len) {
		float realSize;
		if (len > KB && len < MB) {
			realSize = ((float) len / KB);
			String str = String.format("%.2f", realSize);
			return str + " KB";
		} else if (len > MB && len < GB) {
			realSize = ((float) len / MB);
			String str = String.format("%.2f", realSize);
			return str + " MB";
		} else if (len < KB) {
			return Long.toString(len) + " B";
		} else {
			realSize = ((float) len / GB);
			String str = String.format("%.2f", realSize);
			return str + " GB";
		}
	}

}