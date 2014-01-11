package com.dugbel.glass.battery;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Activity that checks the battery status outputs it to a card then reads it
 * aloud
 * 
 * @author dbell
 * 
 */
public class BatteryStatusActivity extends Activity {

	/** */
	private static final String TAG = "BatteryStatusActivity";

	/** */
	private Context context = this;

	/** */
	private TextToSpeech speech;

	/** */
	private String statusText;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		final Intent batteryStatus = context.registerReceiver(null, ifilter);

		// Are we charging / charged?
		final int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		final boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
		final boolean isCharged = status == BatteryManager.BATTERY_STATUS_FULL;

		// How are we charging?
		final int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		final boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		final boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		// Battery Level
		final int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		final int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		final float batteryPct = level / (float) scale;

		statusText = Math.round(batteryPct * 100) + "%";

		// TODO Calculation of drain to hours

		final StringBuilder footnote = new StringBuilder();
		if (isCharging) {
			footnote.append("charging");
			if (usbCharge) {
				footnote.append(" via USB");
			}
			if (acCharge) {
				footnote.append(" via AC");
			}
		} else if (isCharged) {
			footnote.append("fully charged");
		} else if (!isCharging) {
			footnote.append("not charging");
		}

		Card card = new Card(context);
		card.setText(statusText);
		card.setFootnote(footnote.toString());
		card.setImageLayout(Card.ImageLayout.LEFT);

		if (batteryPct == 1.0) {
			card.addImage(R.drawable.ic_battery_100);
			card.setText("Charged");
		} else if (batteryPct > .5) {
			card.addImage(R.drawable.ic_battery_75);
		} else if (batteryPct > .2) {
			card.addImage(R.drawable.ic_battery_50);
		} else {
			card.addImage(R.drawable.ic_battery_20);
		}

		View card1View = card.toView();
		setContentView(card1View);

		speech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				speech.speak(statusText, TextToSpeech.QUEUE_FLUSH, null);
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (speech != null) {
			speech.stop();
			speech.shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			Log.v(TAG, "reading battery state");
			speech.speak(statusText, TextToSpeech.QUEUE_FLUSH, null);
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}
}
