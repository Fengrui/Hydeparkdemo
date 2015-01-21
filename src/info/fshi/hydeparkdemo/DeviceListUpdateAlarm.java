package info.fshi.hydeparkdemo;

import info.fshi.hydeparkdemo.network.WebServerConnector;
import info.fshi.hydeparkdemo.utils.Constants;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class DeviceListUpdateAlarm extends BroadcastReceiver {
	static final String TAG = "DeviceListUpdateAlarm"; /** for logging */   
	private static WakeLock wakeLock; 
	private static final String WAKE_LOCK = "DeviceListUpdateWakeLock";

	private static PendingIntent alarmIntent;

	private static long interval;

	private static WebServerConnector mWebConnector = null;

	public DeviceListUpdateAlarm(){}

	public DeviceListUpdateAlarm(Context context, WebServerConnector webConnector){
		mWebConnector = webConnector;
		scheduleUpdate(context, System.currentTimeMillis());
	}

	/**
	 * Acquire the Wake Lock
	 * @param context
	 */
	public static void getWakeLock(Context context){

		releaseWakeLock();

		PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , WAKE_LOCK); 
		wakeLock.acquire();
	}

	public static void releaseWakeLock(){
		if(wakeLock != null)
			if(wakeLock.isHeld())
				wakeLock.release();
	}


	/**
	 * Stop the scheduled alarm
	 * @param context
	 */
	public static void stopReporting(Context context) {
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, DeviceListUpdateAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmMgr.cancel(alarmIntent);
		
		releaseWakeLock();
	}

	/**
	 * Schedules a device list update
	 * @param time after how many milliseconds (0 for immediately)?
	 */
	public void scheduleUpdate(Context context, long time) {

		Log.d(TAG, "scheduling a new device list update in " + Long.toString( time ));
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, DeviceListUpdateAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		alarmMgr.cancel(alarmIntent);
		
		interval = Constants.WEB_LOCATION_REPORT_INTERVAL;
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, alarmIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// start scan
		if(mWebConnector != null){
			Log.d(TAG, "device list update");
			mWebConnector.updateDeviceList();
		}

		long newInterval = Constants.WEB_LOCATION_DEVICE_LIST_UPDATE_INTERVAL;

		scheduleUpdate(context, System.currentTimeMillis() + newInterval);
	}
}
