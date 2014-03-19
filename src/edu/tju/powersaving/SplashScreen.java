package edu.tju.powersaving;

import edu.tju.powersaving.StartActivity;
import edu.tju.powersaving.utils.PowerSavingConstants;

import edu.tju.powersaving.utils.PowerSavingUtils;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;

public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Called initially to set the orientation to protrait only for mobiles
		// and both for tablets
		PowerSavingUtils.setProtraitOrientationEnabled(SplashScreen.this);

		setContentView(R.layout.activity_splash_screen);

		// Timer to start SearchDeviceActivity
		startTimer();
	}

	/**
	 * Starts the next activity with a little delay
	 */
	private void startTimer() {
		Handler splashTimer = new Handler();
		splashTimer.postDelayed(new Runnable() {
			public void run() {
				Intent configIntent = new Intent(SplashScreen.this,
						StartActivity.class);
				startActivity(configIntent);
				finish();
			}
		}, PowerSavingConstants.CC3X_SPLASH_DELAY);
	}

	/**
	 * gets called in activity when a device is rotated in any side.. so we set
	 * the orientation based on screen size.
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);

		if (!(PowerSavingUtils.isScreenXLarge(getApplicationContext()))) {
			return;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		return true;
	}

	/**
	 * Ovveriding back press function to ignore the back key event when user
	 * press back while processing the Splashscreen wait thread
	 */
	@Override
	public void onBackPressed() {
		return;

	}
}
