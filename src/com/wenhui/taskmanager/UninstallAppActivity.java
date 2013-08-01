package com.wenhui.taskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UninstallAppActivity extends ListActivity {

	private List<PackageInfo> mApps;
	private InstalledAppAdapter adapter;
	private PackageManager pm;
	private ArrayList<ApkInfo> apkInfo = new ArrayList<ApkInfo>();
	private Button buttonUninstall;
	private Button buttonBackup;
	private ListView listview;
	public static final String DEFAULT_DIR_NAME = "SE Task Manager";
	private int totalAppNum = 0;
	private ProgressBar progressbar;
	private TextView textLoading;
	private static boolean finishLoading = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pm = getPackageManager();
		setContentView(R.layout.uninstall_app);
		buttonUninstall = (Button) findViewById(R.id.button_uninstall);
		buttonBackup = (Button) findViewById(R.id.button_backup);
		final CheckBox checkAll = (CheckBox) findViewById(R.id.checkBox_all);
		progressbar = (ProgressBar) findViewById(R.id.progressBar_uninstall);
		textLoading = (TextView) findViewById(R.id.textView_uninstall);
		buttonUninstall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				uninstallApp();
				checkAll.setChecked(false);
			}
		});
		buttonBackup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<String> pkgnames = adapter.checkedApkPackageNames();
				if (pkgnames.isEmpty())
					Log.i("pkgnames empty", "fail");
				String[] packageNames = pkgnames.toArray(new String[pkgnames
						.size()]);
				new BackupAppTask().execute(packageNames);
				checkAll(false);
				checkAll.setChecked(false);
			}
		});
		checkAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				checkAll(isChecked);
			}
		});

		listview = getListView();
		adapter = new InstalledAppAdapter(UninstallAppActivity.this, apkInfo);
		listview.setAdapter(adapter);
		progressbar.setVisibility(View.VISIBLE);
		// new LoadAppsTask().execute();
	}

	private void checkAll(boolean check) {
		for (ApkInfo ai : apkInfo) {
			ai.setChecked(check);
		}
		adapter.notifyDataSetChanged();
	}

	private void fillList() {
		adapter.notifyDataSetChanged();
		new UpdateListTask().execute();
	}

	private class UpdateListTask extends AsyncTask<String, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(String... arg0) {
			try {
				for (ApkInfo ai : apkInfo) {
					try {
						if(!ai.getVersion().equals("")){
							continue;
						}
						String packageName = ai.getPackageName();
						PackageInfo pi;
						pi = (PackageInfo) pm.getPackageInfo(packageName, 0);
						Drawable icon = pi.applicationInfo.loadIcon(pm);
						String label = (String) pi.applicationInfo
								.loadLabel(pm);
						String version = pi.versionName;
						String packagename = pi.packageName.toString();
						int size = 0;
						if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0)
							size = 1;
						ai.setDrawable(icon);
						ai.setLabel(label);
						ai.setVersion(version);
						ai.setSize(size);
						ai.setFile(packagename);
						publishProgress(true);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(Boolean... values) {
			super.onProgressUpdate(values);
			adapter.notifyDataSetChanged();
		}

	}

	private void sortByLabel() {
		Collections.sort(apkInfo, new Comparator<ApkInfo>() {

			@Override
			public int compare(ApkInfo object1, ApkInfo object2) {

				return object1.getLabel().toLowerCase()
						.compareTo(object2.getLabel().toLowerCase());

			}
		});
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(!finishLoading) return;
		ApkInfo info = (ApkInfo) adapter.getItem(position);
		String packagename = info.getPackageName();
		openApkForDetail(packagename);

	}

	private void uninstallApp() {
		ArrayList<String> pkgnames = adapter.checkedApkPackageNames();
		for (String packageName : pkgnames) {
			Intent intent = new Intent(Intent.ACTION_DELETE);
			intent.setData(Uri.parse("package:" + packageName));
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {

			}
		}
	}

	private void openApkForDetail(String packageName) {
		Intent intent;
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			/*
			 * on 2.3 and newer, use APPLICATION_DETAILS_SETTINGS with proper
			 * URI
			 */
			Uri packageURI = Uri.parse("package:" + packageName);
			intent = new Intent(
					"android.settings.APPLICATION_DETAILS_SETTINGS", packageURI);
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {

			}
		} else {
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings",
					"com.android.settings.InstalledAppDetails");
			intent.putExtra("com.android.settings.ApplicationPkgName",
					packageName);
			intent.putExtra("pkg", packageName);
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {

			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (finishLoading)
			new LoadAppsTask().execute();
	}

	private class LoadAppsTask extends AsyncTask<File, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			finishLoading = false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (progressbar.isShown()) {
				progressbar.setVisibility(View.GONE);
				textLoading.setVisibility(View.GONE);
			}
			if (result)
				fillList();
			finishLoading = true;
		}

		@Override
		protected Boolean doInBackground(File... params) {

			mApps = pm.getInstalledPackages(0);
			Log.i("sizeof apkInfo:", Integer.toString(apkInfo.size()));
			try {
				if (mApps.size() > totalAppNum) {
					totalAppNum = mApps.size();
					for (PackageInfo pi : mApps) {
						try {
							if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
									&& (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
								Drawable icon = getResources().getDrawable(
										R.drawable.appicon);
								String label = (String) pi.applicationInfo
										.loadLabel(pm);
								String packagename = pi.packageName;
								ApkInfo ainfo = new ApkInfo(icon, packagename,
										label, 0, "");
								if (!apkInfo.contains(ainfo)) {
									apkInfo.add(ainfo);
								}
							}
						} catch (Exception e) {

						}
					}
					sortByLabel();
					return true;
				} else if (mApps.size() < totalAppNum) {
					totalAppNum = mApps.size();

					for (int index = apkInfo.size() - 1; index >= 0; index--) {
						try {
							ApkInfo ai = apkInfo.get(index);
							pm.getPackageInfo(ai.getPackageName(), 0);
						} catch (NameNotFoundException nnfe) {
							apkInfo.remove(index);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					sortByLabel();
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {

			}
			sortByLabel();
			return true;
		}

	}

	private class BackupAppTask extends AsyncTask<String, String, Boolean> {

		private ProgressDialog dialog = new ProgressDialog(
				UninstallAppActivity.this);

		private int totalApps;
		private int appsBackupSoFar = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			File file = new File(Environment.getExternalStorageDirectory(),
					DEFAULT_DIR_NAME);
			String msg = getString(R.string.backup_msg) + " " + file.getPath();
			dialog.setMessage(msg);
			dialog.show();

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			if (result) {
				Toast.makeText(UninstallAppActivity.this, R.string.done,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(UninstallAppActivity.this,
						R.string.can_not_read_sdcard, Toast.LENGTH_LONG).show();
			}
		}

		private boolean isExternalStorageAval() {
			return Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (!isExternalStorageAval()) {
				return false;
			}
			totalApps = params.length;
			dialog.setMax(totalApps);
			File defaultDir = createDefaultDir();
			for (String pkgname : params) {
				try {
					ApplicationInfo appInfo = pm.getApplicationInfo(pkgname, 0);
					File srcDir = new File(appInfo.sourceDir);
					String label = (String) appInfo.loadLabel(pm);
					publishProgress(label);
					File dst = new File(defaultDir, label + ".apk");
					copyFile(srcDir, dst);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return true;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			appsBackupSoFar++;
			dialog.setMessage(values[0]);
			dialog.setProgress(appsBackupSoFar);

		}

		private File createDefaultDir() {
			File root = Environment.getExternalStorageDirectory();
			File defaultDir = new File(root, DEFAULT_DIR_NAME);
			if (!defaultDir.exists()) {
				if (!defaultDir.mkdirs()) {
				}
			}
			return defaultDir;
		}

		private void copyFile(File src, File dst) throws IOException {
			FileChannel inChannel = new FileInputStream(src).getChannel();
			FileChannel outChannel = new FileOutputStream(dst).getChannel();
			try {
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} finally {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			}
		}

	}

}
