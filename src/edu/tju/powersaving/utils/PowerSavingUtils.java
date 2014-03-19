package edu.tju.powersaving.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class PowerSavingUtils {

	/**
	 * returns true if screen is Xlarge so restricts orientation based on that
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isScreenXLarge(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= (Configuration.SCREENLAYOUT_SIZE_LARGE);
	}

	/**
	 * Sets the orientation enabled to true or false in this case true if tablet
	 * false if mobile device
	 * 
	 * @param activity
	 */
	public static void setProtraitOrientationEnabled(Activity activity) {
		if (!(isScreenXLarge(activity))) {

			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
}
