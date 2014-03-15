package edu.tju.powersaving;

import com.integrity_project.smartconfiglib.FirstTimeConfigListener;

/**
 * A callback listener class implementing FirstTimeConfigListener from Jar file.
 * presently un-used.Can be used in future implementations if any in order to seperate tasks from being written in single class
 * 
 * @author raviteja
 *
 */
public class PowerSavingApiManager implements FirstTimeConfigListener
{
	@Override
	public void onFirstTimeConfigEvent(FtcEvent arg0, Exception arg1)
	{
		arg1.printStackTrace();
		switch (arg0)
		{
		case FTC_ERROR:
			break;
		case FTC_SUCCESS:

			break;
		case FTC_TIMEOUT:
			break;

		default:
			break;
		}
	}

}