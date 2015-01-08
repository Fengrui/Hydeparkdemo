package info.fshi.hydeparkdemo;

import info.fshi.hydeparkdemo.listener.BTConnectButtonOnClickListener;
import info.fshi.hydeparkdemo.network.BTCom;
import info.fshi.hydeparkdemo.network.BTController;
import info.fshi.hydeparkdemo.network.BTScanningAlarm;
import info.fshi.hydeparkdemo.packet.BasicPacket;
import info.fshi.hydeparkdemo.settings.BTSettings;
import info.fshi.hydeparkdemo.utils.Constants;
import info.fshi.hydeparkdemo.utils.SharedPreferencesUtil;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;


public class DeviceListActivity extends Activity {

	boolean mScanning = false;// bool to indicate if scanning
	private static Context mContext;

	// networking
	private static BTController mBTController;

	// list 
	private static ArrayList<Device> deviceList = new ArrayList<Device>();
	private static DeviceListAdapter deviceListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_list);
		mContext = this;

		// necessary init
		initBluetoothUtils();
		registerBroadcastReceivers();

		// update shared preference
		SharedPreferencesUtil.savePreferences(mContext, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_BT_SCAN_DURATION);
		SharedPreferencesUtil.savePreferences(mContext, Constants.SP_RADIO_SCAN_DURATION_ID, Constants.DEFAULT_BT_SCAN_DURATION_ID);
		SharedPreferencesUtil.savePreferences(mContext, Constants.SP_RADIO_SCAN_INTERVAL, Constants.DEFAULT_BT_SCAN_INTERVAL);
		SharedPreferencesUtil.savePreferences(mContext, Constants.SP_RADIO_SCAN_INTERVAL_ID, Constants.DEFAULT_BT_SCAN_INTERVAL_ID);
		SharedPreferencesUtil.savePreferences(mContext, Constants.SP_CHECKBOX_BT_AUTO_SCAN, false);

		deviceListAdapter = new DeviceListAdapter(mContext, R.layout.device, deviceList);
		ListView deviceLv = (ListView) findViewById(R.id.bt_device_list);
		deviceLv.setAdapter(deviceListAdapter);
		deviceLv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				DeviceInfo deviceInfoDialog = new DeviceInfo(mContext, deviceList.get(position));
				deviceInfoDialog.setTitle(Constants.TITLE_DEVICE_INFO_DIALOG);
				deviceInfoDialog.show();
			}
		});
	}


	private void registerBroadcastReceivers(){
		// Register the bluetooth BroadcastReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_UUID);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		registerReceiver(BTFoundReceiver, filter);
	}

	private void unregisterBroadcastReceivers(){
		unregisterReceiver(BTFoundReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device_list, menu);
		if (!mScanning) {
			menu.findItem(R.id.action_stop).setVisible(false);
			menu.findItem(R.id.action_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.action_stop).setVisible(true);
			menu.findItem(R.id.action_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}
		menu.findItem(R.id.bt_set_autoscan_start).setChecked(SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_CHECKBOX_BT_AUTO_SCAN, false));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		BTSettings btSetting;
		int id = item.getItemId();
		switch (id){
		case R.id.action_scan:
			mBTController.startBTScan(true, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_BT_SCAN_DURATION));
			break;
		case R.id.action_stop:
			mBTController.startBTScan(false, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_BT_SCAN_DURATION));
			break;
		case R.id.bt_set_scaninterval:
			btSetting = new BTSettings(mContext, Constants.BT_SCAN_INTERVAL_SETUP_ID);
			btSetting.show("SET SCAN INTERVAL");
			break;
		case R.id.bt_set_scantime:
			btSetting = new BTSettings(mContext, Constants.BT_SCAN_DURATION_SETUP_ID);
			btSetting.show("SET SCAN DURATION");
			break;
		case R.id.bt_set_autoscan_start:
			if(item.isChecked()){ //autoscan will be stopped
				SharedPreferencesUtil.savePreferences(mContext, Constants.SP_CHECKBOX_BT_AUTO_SCAN, false);
				item.setChecked(false);
				stopAutoScanTask();
			}
			else{
				SharedPreferencesUtil.savePreferences(mContext, Constants.SP_CHECKBOX_BT_AUTO_SCAN, true);
				item.setChecked(true);
				startAutoScanTask();
			}
			break;
		default:
			break;
		}
		return true;    }

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		BTScanningAlarm.stopScanning(mContext);
		unregisterBroadcastReceivers();
	}

	// bluetooth part
	private BluetoothAdapter mBluetoothAdapter = null;
	private final int REQUEST_BT_ENABLE = 1;
	private final int REQUEST_BT_DISCOVERABLE = 11;
	private int RESULT_BT_DISCOVERABLE_DURATION = 0;

	private void initBluetoothUtils(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			Toast.makeText(mContext, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
		}
		else{
			// start bluetooth utils
			BTServiceHandler handler = new BTServiceHandler();
			mBTController = new BTController(handler);
			mBTController.startBTServer();
			BTScanningAlarm.stopScanning(mContext);
		}
	}

	@SuppressLint("HandlerLeak") private class BTServiceHandler extends Handler {

		private final String TAG = "BTServiceHandler";
		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			String MAC = b.getString(BTCom.BT_DEVICE_MAC);
			switch(msg.what){
			case BTCom.BT_DATA:
				JSONObject json;
				int type;
				try {
					json = new JSONObject(b.getString(BTCom.BT_DATA_CONTENT));
					type = json.getInt(BasicPacket.PACKET_TYPE);
					switch(type){
					case BasicPacket.PACKET_TYPE_SENSOR_READING: // sensor reading data received
						Log.d(TAG, "receive sensor readings " + json.getString(BasicPacket.PACKET_DATA));
						deviceListAdapter.setDeviceMessage(MAC, json.getString(BasicPacket.PACKET_DATA));
						JSONObject sensorReadingAck = new JSONObject();
						try {
							sensorReadingAck.put(BasicPacket.PACKET_TYPE, BasicPacket.PACKET_TYPE_SENSOR_READING_ACK);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mBTController.sendToBTDevice(MAC, sensorReadingAck);
						break;
					case BasicPacket.PACKET_TYPE_SENSOR_READING_ACK: // acknowledge sensor reading data
						Log.d(TAG, "receive sensor reading ack");
						mBTController.stopConnection(MAC);
						break;
					case BasicPacket.PACKET_TYPE_SENSOR_REQUEST: // request for sensor reading
						Log.d(TAG, "receive sensor readings request, send sensor reading");
						JSONObject sensorReading = new JSONObject();
						try {
							sensorReading.put(BasicPacket.PACKET_TYPE, BasicPacket.PACKET_TYPE_SENSOR_READING);
							sensorReading.put(BasicPacket.PACKET_DATA, "tempature: 100, humidity: 100, this is for test purpose with a very long message");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mBTController.sendToBTDevice(MAC, sensorReading);
						break;
					default:
						break;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case BTCom.BT_CLIENT_ALREADY_CONNECTED:
			case BTCom.BT_CLIENT_CONNECTED:
				Log.d(TAG, "client connected");
				// update main UI (current listview)
				Log.d(Constants.TAG_ACT_TEST, "send sensor reading request");
				JSONObject sensorReadingRequset = new JSONObject();
				try {
					sensorReadingRequset.put(BasicPacket.PACKET_TYPE, BasicPacket.PACKET_TYPE_SENSOR_REQUEST);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mBTController.sendToBTDevice(MAC, sensorReadingRequset);
				deviceListAdapter.updateRetryBackoff(MAC, false);
				deviceListAdapter.setDeviceAction(MAC, BTCom.BT_CLIENT_CONNECTED);
				deviceListAdapter.notifyDataSetChanged();
				break;
			case BTCom.BT_CLIENT_CONNECT_FAILED:
				Log.d(Constants.TAG_ACT_TEST, "client failed");
				if(deviceListAdapter.canRetry(MAC)){
					Log.d(TAG, "retry to connect to " + MAC);
					mBTController.connectBTServer(deviceListAdapter.getDevice(MAC).btDevice.btRawDevice, Constants.BT_CLIENT_TIMEOUT);
				}
				else{
					deviceListAdapter.updateRetryBackoff(MAC, true);
					deviceListAdapter.setDeviceAction(MAC, BTCom.BT_CLIENT_CONNECT_FAILED);
					deviceListAdapter.notifyDataSetChanged();
				}
				break;
			case BTCom.BT_DISCONNECTED:
				Log.d(Constants.TAG_ACT_TEST, "disconnected");
				deviceListAdapter.setDeviceAction(MAC, BTCom.BT_DISCONNECTED);
				deviceListAdapter.notifyDataSetChanged();
				break;
			case BTCom.BT_SERVER_CONNECTED:
				Log.d(TAG, "server connected");
				deviceListAdapter.updateRetryBackoff(MAC, false);
				deviceListAdapter.setDeviceAction(MAC, BTCom.BT_CLIENT_CONNECTED);
				deviceListAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
	}

	// timestamp to control if it is a new scan
	private long scanStartTimestamp = System.currentTimeMillis() - 100000;

	// Create a BroadcastReceiver for actions
	BroadcastReceiver BTFoundReceiver = new BTServiceBroadcastReceiver();

	class BTServiceBroadcastReceiver extends BroadcastReceiver {

		ArrayList<String> devicesFoundStringArray = new ArrayList<String>();

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) { // check if one device found more than once
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String deviceMac = device.getAddress();
				if(!devicesFoundStringArray.contains(deviceMac)){
					devicesFoundStringArray.add(deviceMac);
					Log.d(Constants.TAG_APPLICATION, "get a device : " + String.valueOf(deviceMac));
					/*
					 * -30dBm = Awesome
					 * -60dBm = Good
					 * -80dBm = OK
					 * -90dBm = Bad
					 */
					short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);

					int index = deviceListAdapter.deviceIndex(deviceMac);
					if (index < 0){ // device not exist
						BTDevice btDevice = new BTDevice(device);
						btDevice.rssi = rssi;
						btDevice.name = device.getName();
						btDevice.connState = Constants.STATE_CLIENT_UNCONNECTED;
						btDevice.btConnect = new BTConnectButtonOnClickListener(btDevice, mBTController);
						Device newDevice = new Device();
						newDevice.btDevice = btDevice;
						deviceList.add(newDevice);
					}
					else{ // device already found
						Device myDevice = deviceListAdapter.getItem(index);
						myDevice.btDevice.rssi = rssi;
						myDevice.btDevice.resetRetryCounter();
						myDevice.btDevice.name = device.getName();
						if(myDevice.btDevice.connState != Constants.STATE_CLIENT_CONNECTED){
							myDevice.btDevice.connState = Constants.STATE_CLIENT_UNCONNECTED;
						}
						myDevice.btDevice.btConnect = new BTConnectButtonOnClickListener(myDevice.btDevice, mBTController);
					}
					deviceListAdapter.sortList();
					deviceListAdapter.notifyDataSetChanged();
				}
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				mScanning = true;
				if(System.currentTimeMillis() - scanStartTimestamp > SharedPreferencesUtil.loadSavedPreferences(context, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_BT_SCAN_DURATION)){
					//a new scan has been started
					Log.d(Constants.TAG_APPLICATION, "Discovery process has been started: " + String.valueOf(System.currentTimeMillis()));
					for (Device device : deviceList){
						if(device.btDevice.connState != Constants.STATE_CLIENT_CONNECTED){
							device.btDevice.connState = Constants.STATE_CLIENT_OUTDATED;
							deviceListAdapter.notifyDataSetChanged();
						}
					}
					devicesFoundStringArray = new ArrayList<String>();
					scanStartTimestamp = System.currentTimeMillis();
				}
				invalidateOptionsMenu();
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				if(mScanning){
					mScanning = false;
					invalidateOptionsMenu();
					Log.d(Constants.TAG_APPLICATION, "Discovery process has been stopped: " + String.valueOf(System.currentTimeMillis()));
					new ReadSensorData().execute();
				}
			}
		}
	};

	/**
	 * exchange neighbor table
	 * @author fshi
	 *
	 */
	private class ReadSensorData extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... voids) {
			// init the counter

			for(Device device : deviceListAdapter.getConnectableDevices()){
				mBTController.connectBTServer(device.btDevice.btRawDevice, Constants.BT_CLIENT_TIMEOUT);
			}
			return null;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode){
		case REQUEST_BT_ENABLE:
			if (resultCode == RESULT_OK) {
				// start bluetooth utils
				initBluetoothUtils();
				Intent discoverableIntent = new
						Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, RESULT_BT_DISCOVERABLE_DURATION);
				startActivityForResult(discoverableIntent, REQUEST_BT_DISCOVERABLE);
			}
			else{
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is not enabled by the user.");
			}
			break;
		case REQUEST_BT_DISCOVERABLE:
			if (resultCode == RESULT_CANCELED){
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is not discoverable.");
			}
			else{
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is discoverable by 300 seconds.");
			}
			break;
		default:
			break;
		}
	}

	/**
	 * start automatic bluetooth scan, scanning alarm
	 */
	private void startAutoScanTask() {
		new BTScanningAlarm(mContext, mBTController);
	}

	private void stopAutoScanTask() {
		BTScanningAlarm.stopScanning(mContext);
	}

}
