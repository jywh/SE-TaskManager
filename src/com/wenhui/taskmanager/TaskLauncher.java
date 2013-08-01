package com.wenhui.taskmanager;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TaskLauncher extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TabHost tabHost = getTabHost();
		Resources resource = getResources();

		tabHost.addTab(tabHost
				.newTabSpec("tab1")
				.setIndicator(getString(R.string.running),
						resource.getDrawable(R.drawable.tab1_select))
				.setContent(new Intent(this, RunningApplicationActivity.class)));

		tabHost.addTab(tabHost
				.newTabSpec("tab2")
				.setIndicator(getString(R.string.install),
						resource.getDrawable(R.drawable.tab2_select))
				.setContent(new Intent(this, InstallAppActivity.class)));
		tabHost.addTab(tabHost
				.newTabSpec("tab3")
				.setIndicator(getString(R.string.uninstall_backup),
						resource.getDrawable(R.drawable.tab3_select))
				.setContent(new Intent(this, UninstallAppActivity.class)));
		tabHost.addTab(tabHost
				.newTabSpec("tab4")
				.setIndicator(getString(R.string.home_switch),resource.getDrawable(R.drawable.tab4_select))
				.setContent(new Intent(this, HomeSwitchActivity.class)));
		tabHost.addTab(tabHost
				.newTabSpec("tab5")
				.setIndicator(getString(R.string.summary), resource.getDrawable(R.drawable.tab5_select))
				.setContent(new Intent(this, DisplaySummaryActivity.class)));
	}
	
	

}
