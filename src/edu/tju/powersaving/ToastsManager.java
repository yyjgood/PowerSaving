package edu.tju.powersaving;

import edu.tju.powersaving.utils.PowerSavingConstants;
import android.content.Context;
import android.widget.Toast;

public class ToastsManager {
	/**
	 * Called activity context
	 */
	private Context mContext=null;
	
	private Toast mToast=null;
	
	public ToastsManager(Context context){
		this.mContext = context;
	}
	
	
	private String text = null;
	
	

	
	
	public void showCustomAlertDialog(int dialogType){
		switch (dialogType) 
		{

		case PowerSavingConstants.DLG_NO_WIFI_AVAILABLE:
			this.text = mContext.getResources().getString(R.string.alert_no_network_title);

			break;
		
		case PowerSavingConstants.DLG_CONNECTION_SUCCESS:
			this.text = mContext.getResources().getString(R.string.alert_successfully_connected);

			break;

		case PowerSavingConstants.DLG_CONNECTION_FAILURE:
			this.text = mContext.getResources().getString(R.string.alert_connection_failed);

			break;

		case PowerSavingConstants.DLG_CONNECTION_TIMEOUT:
			this.text = mContext.getResources().getString(R.string.alert_connection_timeout);

			break;

			
		case PowerSavingConstants.DLG_SSID_INVALID:
			this.text = mContext.getResources().getString(R.string.alert_no_network_title);
			break;

		case PowerSavingConstants.DLG_GATEWAY_IP_INVALID:
			this.text = mContext.getResources().getString(R.string.alert_no_network_title);
			break;

		case PowerSavingConstants.DLG_KEY_INVALID:
			this.text = mContext.getResources().getString(R.string.alert_invalid_key_mesg);
			break;

		case PowerSavingConstants.DLG_PASSWORD_INVALID:
			this.text = mContext.getResources().getString(R.string.alert_no_network_title);
			break;


		}
		
		
		if(mToast == null)  
        {  
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);  
        }  
        else {  
            mToast.setText(text);  
        } 
		mToast.show();
	}

}
