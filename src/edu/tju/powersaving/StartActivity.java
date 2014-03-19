package edu.tju.powersaving;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
	}

	public void searchDeviceHandler(View source) {
		Intent configIntent = new Intent(StartActivity.this,
				SearchDeviceActivity.class);
		startActivity(configIntent);
	}

}
