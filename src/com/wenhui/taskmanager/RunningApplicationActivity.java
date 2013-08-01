package com.wenhui.taskmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RunningApplicationActivity extends ListActivity {

	private ListView listview;
	private ActivityManager am;
	private RunningAppAdapter adapter;
	private ArrayList<AppInfo> listInfo = new ArrayList<AppInfo>();
	private Button buttonEndAll;
	private Button buttonEnd;
	private Button buttonRefresh;
	private ProgressBar progressbar;
	private HashMap<Integer, String> statusMap = new HashMap<Integer, String>();
	private IgnoreProcessDB mDb;
	private ArrayList<String> ignoredPkg = new ArrayList<String>();
	private static int SDK_INT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.list_running_app);
		SDK_INT = android.os.Build.VERSION.SDK_INT;
		mDb = new IgnoreProcessDB(this);
		ignoredPkg = mDb.getIgnoredPackage();
		listview = getListView();
		listview.setOnCreateContextMenuListener(this);
		buttonEndAll = (Button) findViewById(R.id.button_end_all);
		buttonEnd = (Button) findViewById(R.id.button_end);
		buttonRefresh = (Button) findViewById(R.id.button_refresh);
		progressbar = (ProgressBar) findViewById(R.id.progressBar1);
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		buttonRefresh.setEnabled(false);
		buttonEnd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (adapter == null)
					return;
				ArrayList<Integer> ifs = adapter.getSelectedItemsPosition();
				if(ifs.isEmpty())
					return;
				for (int i=ifs.size()-1; i>=0; i--) {
					int index=ifs.get(i);
					if (!listInfo.get(index).isIgnored()) {
						String packageName = listInfo.get(index).getPckname();
						if (SDK_INT >= 8)
							am.killBackgroundProcesses(packageName);
						else
							am.restartPackage(packageName);
						listInfo.remove(index);
					}
				}
				adapter.notifyDataSetChanged();
			}
		});

		buttonEndAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (AppInfo ai : listInfo) {
					if (!ai.isIgnored()) {
						String packagename = ai.getPckname();
						if (SDK_INT >= 8)
							am.killBackgroundProcesses(packagename);
						else
							am.restartPackage(packagename);
					}
				}
				new UpdateListTask().execute();
			}
		});

		buttonRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonRefresh.setEnabled(false);
				new UpdateListTask().execute();
			}
		});
		progressbar.setVisibility(View.VISIBLE);
		new UpdateListTask().execute();

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try {
			if (listInfo.get(position).isIgnored())
				return;
			CheckBox select = (CheckBox) v.findViewById(R.id.checkBox_select);
			boolean newValue = !adapter.getItem(position).getIsSelected();
			select.setChecked(newValue);
			adapter.getItem(position).setSelected(newValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		String pkgname = adapter.getItem(info.position).getPckname();
		switch (item.getItemId()) {
		case R.id.ignore:
			new insertIgnoreDbTask().execute(pkgname);
			ignoredPkg.add(pkgname);
			listInfo.get(info.position).setIgnore(true);
			adapter.notifyDataSetChanged();
			break;
		case R.id.clear_ignore:
			new clearIgnoreDbTask().execute(pkgname);
			ignoredPkg.remove(pkgname);
			listInfo.get(info.position).setIgnore(false);
			adapter.notifyDataSetChanged();
			break;
		default:

		}
		return true;
	}

	private class insertIgnoreDbTask extends
			AsyncTask<String, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			mDb.insertIgnorePkgname(params[0]);
			return true;
		}

	}

	private class clearIgnoreDbTask extends AsyncTask<String, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			mDb.clearIgnorePkgname(params[0]);
			return true;
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(R.string.complete_action_using);
		String pkgname = adapter.getItem(info.position).getPckname();
		if (ignoredPkg.contains(pkgname))
			getMenuInflater().inflate(R.menu.clear_ignore, menu);
		else
			getMenuInflater().inflate(R.menu.ignore, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.running_app_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_all_ignore:
			new ClearAllIgnoreTask().execute();
			for (AppInfo ai : listInfo) {
				ai.setIgnore(false);
			}
			ignoredPkg.clear();
			adapter.notifyDataSetChanged();
			break;
		}
		return true;
	}

	private class ClearAllIgnoreTask extends
			AsyncTask<String, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			mDb.clearAllIgnorePkg();
			return true;
		}

	}

//	private void fillList() {
//		adapter = new RunningAppAdapter(this, listInfo);
//		listview.setAdapter(adapter);
//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDb != null)
			mDb.close();

	}

	private class UpdateListTask extends AsyncTask<Boolean, String, Boolean> {

		ArrayList<AppInfo> newList = new ArrayList<AppInfo>();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			init();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			listInfo = newList;
			if (progressbar.isShown())
				progressbar.setVisibility(View.GONE);
			adapter = new RunningAppAdapter(RunningApplicationActivity.this, listInfo);
			listview.setAdapter(adapter);
			if (!buttonRefresh.isEnabled())
				buttonRefresh.setEnabled(true);
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			List<ActivityManager.RunningAppProcessInfo> l = am
					.getRunningAppProcesses();
			Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
			PackageManager pm = getPackageManager();
			int[] pids = new int[1];
			MemoryInfo[] mi = new MemoryInfo[1];
			while (i.hasNext()) {
				ActivityManager.RunningAppProcessInfo info = i.next();
				try {
					if(info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE ||
							info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
						continue;
					ApplicationInfo appInfo = pm.getApplicationInfo(
							info.processName, 0);
					if ((appInfo.flags & ApplicationInfo.FLAG_PERSISTENT) == 0) {
						CharSequence appLabel = pm.getApplicationLabel(appInfo);
						Drawable icon = pm.getApplicationIcon(appInfo);
						pids[0] = info.pid;
						mi = am.getProcessMemoryInfo(pids);
						long usedMemory = mi[0].getTotalPss();
						String memoryUsage = String.format("%.2f",
								((float) usedMemory / 1024)) + "MB";
						String status = getStatus(info.importance);
						String pkgname = appInfo.packageName;
						boolean isIgnored = ignoredPkg.contains(pkgname);
						newList.add(new AppInfo(icon, (String) appLabel,
								memoryUsage, pkgname, pids[0], status,
								isIgnored));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			sortByLabel(newList);
			return true;
		}

		private void init() {
			statusMap
					.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND,
							getString(R.string.foreground));
			statusMap.put(
					ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE,
					getString(R.string.visible));
			statusMap.put(
					ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE,
					getString(R.string.service));
			statusMap
					.put(ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND,
							getString(R.string.background));
			statusMap.put(
					ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY,
					getString(R.string.empty));
		}

		private String getStatus(int key) {
			String status = statusMap.get(key);
			if (status != null)
				return status;
			else
				return "";
		}

		private ArrayList<AppInfo> sortByLabel(ArrayList<AppInfo> list) {
			try {
				Collections.sort(list, new Comparator<AppInfo>() {

					@Override
					public int compare(AppInfo object1, AppInfo object2) {

						return object1.getLabel().toLowerCase()
								.compareTo(object2.getLabel().toLowerCase());

					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			return list;
		}

	}

	private class RunningAppAdapter extends BaseAdapter {

		class ViewHolder {
			ImageView icon;
			TextView label;
			TextView usage;
			TextView status;
			TextView ignore;
		}

		private List<AppInfo> appInfo;
		private LayoutInflater inflater;

		public RunningAppAdapter(Context context, List<AppInfo> appInfo) {
			inflater = LayoutInflater.from(context);
			this.appInfo = appInfo;
		}

		@Override
		public int getCount() {
			return appInfo.size();
		}

		@Override
		public AppInfo getItem(int arg0) {
			return appInfo.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return appInfo.get(arg0).getPid();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {

			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.running_app, null);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.imageView_app_icon);
				holder.label = (TextView) convertView
						.findViewById(R.id.textView_app_name);
				holder.status = (TextView) convertView
						.findViewById(R.id.textView_app_info1);
				holder.usage = (TextView) convertView
						.findViewById(R.id.textView_app_info2);
				holder.ignore = (TextView) convertView
						.findViewById(R.id.textView_ignore);
				holder.icon.setAdjustViewBounds(true);
				holder.icon.setScaleType(ScaleType.CENTER_INSIDE);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final AppInfo info = appInfo.get(position);
			holder.icon.setImageDrawable(info.getIcon());
			holder.label.setText(info.getLabel());
			holder.usage.setText(info.getMemory());
			holder.status.setText(info.getStatus());

			final CheckBox select = (CheckBox) convertView
					.findViewById(R.id.checkBox_select);
			select.setFocusable(false);
			select.setChecked(listInfo.get(position).getIsSelected());
			boolean isIgnored = appInfo.get(position).isIgnored();
			if (isIgnored) {
				holder.ignore.setVisibility(View.VISIBLE);
				select.setVisibility(View.GONE);
			} else {
				holder.ignore.setVisibility(View.GONE);
				select.setVisibility(View.VISIBLE);
			}

			select.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					boolean b = !listInfo.get(position).getIsSelected();
					select.setChecked(b);
					listInfo.get(position).setSelected(b);
				}
			});

			return convertView;
		}

		public ArrayList<Integer> getSelectedItemsPosition() {
			ArrayList<Integer> ifs = new ArrayList<Integer>();
			for (int i = 0; i < appInfo.size(); i++) {
				if (appInfo.get(i).getIsSelected()) {
					ifs.add(i);
				}
			}
			return ifs;
		}
	}

}
