package edu.tju.powersaving;

import java.util.ArrayList;
import edu.tju.powersaving.ControlDeviceActivity.IntraDeviceCommunicator;
import edu.tju.powersaving.utils.Appliance;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.RectF;

public class LightDialog extends Dialog implements OnClickListener {

	static byte LightLevel = 0;
	private boolean blink;
	private int blink_count = 0;

	RectF SelectionRect;

	private Button LightDoneButton;
	private SeekBar LightSeekBar;

	protected static final int Update_UI = 0;

	SeekBarMonitor seek_monitor = new SeekBarMonitor();

	IntraDeviceCommunicator IDComm;

	// Blinker Blinky;

	public LightDialog(Context _context, ArrayList<Appliance> L, RectF rect) {
		super(_context);
		SelectionRect = rect;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_light_dialog);

		WindowManager.LayoutParams DialogParams;
		DialogParams = getWindow().getAttributes();
		DialogParams.height = 600;
		DialogParams.width = 566;
		DialogParams.dimAmount = .3333f;
		getWindow().setAttributes(DialogParams);

		LightDoneButton = (Button) findViewById(R.id.LightDoneButton);
		LightDoneButton.setOnClickListener(this);
		LightSeekBar = (SeekBar) findViewById(R.id.LightSeekBar);
		LightSeekBar.setOnSeekBarChangeListener(seek_monitor);
		LightSeekBar.setMax(255);
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.LightDoneButton:
			dismiss();
			break;
		case R.id.spinnertext1:

			break;
		}
	}

	private class SeekBarMonitor implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			LightLevel = (byte) seekBar.getProgress();

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

	}

	public void HandleIDMessage(int dest, int command) {
		if (command == 1) {
			blink = true;
		}
		if (command == 0) {
			blink = false;
		}

	}

	RectF GetSelectionRect() {
		return SelectionRect;
	}

	@SuppressLint("HandlerLeak")
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Update_UI) {
				PublicSetUI();
			}
			super.handleMessage(msg);
		}
	};

	public void PublicSetUI() {

	}

	public void Updatebuffer(byte[] outbuffer) {
		if (blink) {
			blink_count++;
			if (blink_count == 3)
				outbuffer[3] = 0;
			if (blink_count >= 6) {
				outbuffer[3] = (byte) 255;
				blink_count = 0;
			}
		} else
			outbuffer[3] = (byte) (LightLevel);
	}
}
