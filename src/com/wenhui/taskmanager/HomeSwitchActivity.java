package com.wenhui.taskmanager;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class HomeSwitchActivity extends ListActivity {

	protected static final int ACTIVITY_CLEAR_DEFAULT = 4;
	private Button buttonClear;
	private TextView textDefault;
	private ImageView imageDefault;
	private PackageManager pm;
	private ArrayList<HomeItem> homes = new ArrayList<HomeItem>();
	private HomeAdapter adapter;
	private String packageName;
	private Intent intent;
	private static boolean finishLoading=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_switcher);

		buttonClear = (Button) findViewById(R.id.button_clear);
		textDefault = (TextView) findViewById(R.id.textView_default);
		imageDefault = (ImageView) findViewById(R.id.imageView_default);
		pm = getPackageManager();
		intent = new Intent();
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setAction(Intent.ACTION_MAIN);
		buttonClear.setOnClickListener(clearDefault);
	}

	private OnClickListener clearDefault = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent;
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				Uri packageURI = Uri.parse("package:" + packageName);
				intent = new Intent(
						"android.settings.APPLICATION_DETAILS_SETTINGS",
						packageURI);
				startActivity(intent);
			} else {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName("com.android.settings",
						"com.android.settings.InstalledAppDetails");
				intent.putExtra("com.android.settings.ApplicationPkgName",
						packageName);
				intent.putExtra("pkg", packageName);
				startActivity(intent);
			}
		}
	};

	private void findDefault(Intent intent) {

		ResolveInfo ri = pm.resolveActivity(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (ri != null) {
			ApplicationInfo info = ri.activityInfo.applicationInfo;
			packageName = info.packageName;
			if (packageName.equals("android")) {
				noDefaultHomeFound();
			} else {
				Drawable icon = info.loadIcon(pm);
				String label = (String) info.loadLabel(pm);
				imageDefault.setVisibility(View.VISIBLE);
				buttonClear.setVisibility(View.VISIBLE);
				imageDefault.setImageDrawable(icon);
				textDefault.setText(label);
			}
		} else {
			noDefaultHomeFound();
		}
	}

	private class UpdateInfoTask extends AsyncTask<String, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			for (HomeItem hi : homes) {
				try {
					if(!hi.getLable().equals("")){
						continue;
					}
					ApplicationInfo info = pm.getApplicationInfo(
							hi.getPackagename(), 0);
					Drawable icon = info.loadIcon(pm);
					String label = (String) info.loadLabel(pm);
					hi.setIcon(icon);
					hi.setLabel(label);
					publishProgress(true);
				} catch (Exception e) {

				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Boolean... values) {
			super.onProgressUpdate(values);
			adapter.notifyDataSetChanged();
		}

	}

	private void fillList() {
		adapter = new HomeAdapter();
		getListView().setAdapter(adapter);
		new UpdateInfoTask().execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(finishLoading)
			new LoadHomeAppTask().execute();
		findDefault(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home_switcher_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			new LoadHomeAppTask().execute();
			break;
		}
		return true;
	}

	private void noDefaultHomeFound() {
		imageDefault.setVisibility(View.GONE);
		buttonClear.setVisibility(View.GONE);
		textDefault.setText(R.string.no_default_found);
	}

	private class LoadHomeAppTask extends AsyncTask<Intent, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			finishLoading=false;
		}

		@Override
		protected Boolean doInBackground(Intent... params) {

			List<ResolveInfo> ris = pm.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
			try {
				if (ris.size() > homes.size()) {
					for (ResolveInfo ri : ris) {
						try {
							ActivityInfo ai = ri.activityInfo;
							Drawable icon = getResources().getDrawable(
									R.drawable.appicon);
							String packagename = ai.packageName;
							String classname = ai.name;
							HomeItem hi = new HomeItem(icon, "", packagename,
									classname);
							if (!homes.contains(hi)) {
								homes.add(hi);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else if (ris.size() < homes.size()) {
					for (int index = homes.size() - 1; index >= 0; index--) {
						try {
							String packagename = homes.get(index)
									.getPackagename();
							pm.getApplicationInfo(packagename, 0);
						} catch (NameNotFoundException nnfe) {
							homes.remove(index);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result)
				fillList();
			finishLoading=true;
		}

	}

	private class HomeItem {
		private String label;
		private String packagename;
		private Drawable icon;
		private String classname;

		public HomeItem(Drawable icon, String label, String packagename,
				String classname) {
			this.icon = icon;
			this.label = label;
			this.packagename = packagename;
			this.classname = classname;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getLable() {
			return label;
		}

		public String getPackagename() {
			return packagename;
		}

		public void setIcon(Drawable icon) {
			this.icon = icon;
		}

		public Drawable getIcon() {
			return icon;
		}

		public String getClassname() {
			return classname;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (!(o instanceof HomeItem)) {
				return false;
			}

			HomeItem that = (HomeItem) o;
			return that.getPackagename().equals(this.getPackagename());
		}

	}

	private class HomeAdapter extends BaseAdapter {

		private LayoutInflater inflater = LayoutInflater
				.from(getApplicationContext());

		private class ViewHolder {
			TextView label;
			ImageView icon;
			TextView pkgname;
			Button Launch;
		}

		@Override
		public int getCount() {
			return homes.size();
		}

		@Override
		public HomeItem getItem(int arg0) {
			return homes.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.homw_switcher_panel,
						null);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.imageView_icon1);
				holder.icon.setAdjustViewBounds(true);
				holder.icon.setScaleType(ScaleType.CENTER_INSIDE);
				holder.label = (TextView) convertView
						.findViewById(R.id.textView_filename1);
				holder.Launch = (Button) convertView
						.findViewById(R.id.button_launch);
				holder.pkgname = (TextView) convertView
						.findViewById(R.id.textView_pkgname);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.icon.setImageDrawable(getItem(position).getIcon());
			holder.label.setText(getItem(position).getLable());
			holder.pkgname.setText(getItem(position).getPackagename());
			holder.Launch.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String packagename = getItem(position).getPackagename();
					String classname = getItem(position).getClassname();
					Intent launchIntent = new Intent(Intent.ACTION_MAIN);
					launchIntent.setComponent(new ComponentName(packagename,
							classname));
					launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					try {
						startActivity(launchIntent);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
			return convertView;
		}

	}

}
