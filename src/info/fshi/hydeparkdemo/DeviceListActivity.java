package info.fshi.hydeparkdemo;

import info.fshi.hydeparkdemo.data.QueueManager;
import info.fshi.hydeparkdemo.data.TransactionData;
import info.fshi.hydeparkdemo.listener.BTConnectButtonOnClickListener;
import info.fshi.hydeparkdemo.location.DeviceLocationManager;
import info.fshi.hydeparkdemo.location.DeviceLocationUpdateAlarm;
import info.fshi.hydeparkdemo.network.BTCom;
import info.fshi.hydeparkdemo.network.BTController;
import info.fshi.hydeparkdemo.network.BTScanningAlarm;
import info.fshi.hydeparkdemo.network.WebServerConnector;
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

	private final static String TAG = "DeviceListActivity";
	
	// networking
	private static BTController mBTController;

	// device list 
	private static ArrayList<Device> deviceList = new ArrayList<Device>();
	private static DeviceListAdapter deviceListAdapter;

	WebServerConnector mWebConnector;
	
	DeviceLocationManager mDeviceLocationManager;
	
	QueueManager myQueue;
	
	// bluetooth part
	private BluetoothAdapter mBluetoothAdapter = null;
	private final int REQUEST_BT_ENABLE = 1;
	private final int REQUEST_BT_DISCOVERABLE = 11;
	private int RESULT_BT_DISCOVERABLE_DURATION = 0;

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
		mWebConnector = new WebServerConnector();
		mDeviceLocationManager = new DeviceLocationManager(mContext, mWebConnector);
		startLocationReportingTask();
		startDeviceListUpdateTask();
		myQueue = new QueueManager();
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
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
		stopAutoScanTask();
		stopLocationReportingTask();
		stopDeviceListUpdateTask();
		unregisterBroadcastReceivers();
	}

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

		private class ClientConnectionTask extends AsyncTask<String, Void, Void> {
			protected Void doInBackground(String... strings) {
				// init the counter
				String MAC = strings[0];
				int serverType = DeviceList.devices.get(MAC);
				if(serverType == DeviceList.DEVICE_TYPE_RELAY){ // if device is a relay, exchange queue size first
					JSONObject queueSize = new JSONObject();
					try {
						queueSize.put(BasicPacket.PACKET_TYPE, BasicPacket.PACKET_TYPE_QUEUE_SIZE);
						queueSize.put(BasicPacket.PACKET_DATA, myQueue.getQueueLength());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mBTController.sendToBTDevice(MAC, queueSize);
				}else if(serverType == DeviceList.DEVICE_TYPE_SINK){ // if device is a sink, send data
					Log.d(Constants.TAG_ACT_TEST, "send data to sink");
					mBTController.sendToBTDevice(MAC, myQueue.getFromQueue());
				}else{ // if device is a sensor, do nothing here and wait for data
				}
				return null;
			}
		}
		
		private class ServerConnectionTask extends AsyncTask<String, Void, Void> {
			protected Void doInBackground(String... strings) {
				// init the counter
				String MAC = strings[0];
				int myType = DeviceList.devices.get(mBluetoothAdapter.getAddress());
				if(myType == DeviceList.DEVICE_TYPE_RELAY){ // if my device is a relay, exchange queue size
					JSONObject queueSize = new JSONObject();
					try {
						queueSize.put(BasicPacket.PACKET_TYPE, BasicPacket.PACKET_TYPE_QUEUE_SIZE);
						queueSize.put(BasicPacket.PACKET_DATA, myQueue.getQueueLength());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mBTController.sendToBTDevice(MAC, queueSize);
				}else if(myType == DeviceList.DEVICE_TYPE_SENSOR){ // if my device is a sensor, send data
					Log.d(Constants.TAG_ACT_TEST, "send data to relay " + myQueue.getQueueLength());
					mBTController.sendToBTDevice(MAC, myQueue.getFromQueue());
				}else{ // if device is a sink, do nothing here and wait for data
				}
				return null;
			}
		}
		
		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			String MAC = b.getString(BTCom.BT_DEVICE_MAC);
			switch(msg.what){
			case BTCom.BT_CLIENT_ALREADY_CONNECTED:
			case BTCom.BT_CLIENT_CONNECTED:
				deviceListAdapter.setDeviceAction(MAC, BTCom.BT_CLIENT_CONNECTED);
				deviceListAdapter.notifyDataSetChanged();
				Log.d(TAG, "client connected");
				// update main UI (current listview)
				new ClientConnectionTask().execute(MAC);
				break;
			case BTCom.BT_CLIENT_CONNECT_FAILED:
				Log.d(Constants.TAG_ACT_TEST, "client failed");
				if(deviceListAdapter.canRetry(MAC)){
					Log.d(TAG, "retry to connect to " + MAC);
					mBTController.connectBTServer(deviceListAdapter.getDevice(MAC).btRawDevice, Constants.BT_CLIENT_TIMEOUT);
				}
				else{
					deviceListAdapter.setDeviceAction(MAC, BTCom.BT_CLIENT_CONNECT_FAILED);
					deviceListAdapter.notifyDataSetChanged();
				}
				break;
			case BTCom.BT_SUCCESS: // triggered by receiver
				Log.d(Constants.TAG_ACT_TEST, "success");
				deviceListAdapter.setDeviceAction(MAC, BTCom.BT_SUCCESS);
				deviceListAdapter.notifyDataSetChanged();
				break;
			case BTCom.BT_DISCONNECTED:
				Log.d(Constants.TAG_ACT_TEST, "disconnected");
				deviceListAdapter.setDeviceAction(MAC, BTCom.BT_DISCONNECTED);
				deviceListAdapter.notifyDataSetChanged();
				break;
			case BTCom.BT_SERVER_CONNECTED:
				Log.d(TAG, "server connected");
				deviceListAdapter.setDeviceAction(MAC, BTCom.BT_SERVER_CONNECTED);
				deviceListAdapter.notifyDataSetChanged();
				// mainly used for test, in the real case, client is always a relay
				new ServerConnectionTask().execute(MAC);
				break;
			case BTCom.BT_DATA:
				JSONObject json;
				int type;
				try{
					json = new JSONObject(b.getString(BTCom.BT_DATA_CONTENT));
					type = json.getInt(BasicPacket.PACKET_TYPE);
					switch(type){
					case BasicPacket.PACKET_TYPE_QUEUE_SIZE: // sensor reading data received
						Log.d(TAG, "receive queue size " + json.getInt(BasicPacket.PACKET_DATA));
						int queueDiff = (myQueue.getQueueLength() - json.getInt(BasicPacket.PACKET_DATA)) / 2;
						if(queueDiff > 0){
							mBTController.sendToBTDevice(MAC, myQueue.getFromQueue(queueDiff));
						}else if(queueDiff == 0){
							mBTController.stopConnection(MAC);
						}
						break;
					default:
						break;
					}
				}catch (JSONException e) {
					// TODO Auto-generated catch block
					// receive a raw string data
					String data = b.getString(BTCom.BT_DATA_CONTENT);
					Log.d(TAG, "old queue size " + myQueue.getQueueLength());
					myQueue.appendToQueue(data);
					deviceListAdapter.setDeviceMessage(MAC, "receive queue size : " + data.length());
					Log.d(TAG, "new queue size " + myQueue.getQueueLength());
					// prepare data for server
					TransactionData txData = new TransactionData();
					txData.senderAddr = MAC;
					txData.receiverAddr = mBluetoothAdapter.getAddress();
					txData.packetSize = data.length();
					txData.cost = data.length() * 1.5;
					txData.timestamp = System.currentTimeMillis();
					mWebConnector.reportTransactionData(txData);
					mBTController.stopConnection(MAC);
				}
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
				if(DeviceList.devices.containsKey(deviceMac)){ // only respond when a device is in the list
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
							Device btDevice = new Device(device);
							btDevice.rssi = rssi;
							btDevice.name = device.getName();
							btDevice.connState = Constants.STATE_CLIENT_UNCONNECTED;
							btDevice.btConnect = new BTConnectButtonOnClickListener(btDevice, mBTController);
							deviceList.add(btDevice);
						}
						else{ // device already found
							Device myDevice = deviceListAdapter.getItem(index);
							myDevice.rssi = rssi;
							myDevice.resetRetryCounter();
							myDevice.name = device.getName();
							if(myDevice.connState != Constants.STATE_CLIENT_CONNECTED){
								myDevice.connState = Constants.STATE_CLIENT_UNCONNECTED;
							}
							myDevice.btConnect = new BTConnectButtonOnClickListener(myDevice, mBTController);
						}
						deviceListAdapter.sortList();
						deviceListAdapter.notifyDataSetChanged();
					}
				}
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				mScanning = true;
				if(System.currentTimeMillis() - scanStartTimestamp > SharedPreferencesUtil.loadSavedPreferences(context, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_BT_SCAN_DURATION)){
					//a new scan has been started
					Log.d(Constants.TAG_APPLICATION, "Discovery process has been started: " + String.valueOf(System.currentTimeMillis()));
					for (Device device : deviceList){
						if(device.connState != Constants.STATE_CLIENT_CONNECTED){
							device.connState = Constants.STATE_CLIENT_OUTDATED;
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
					new ExchangeData().execute();
				}
			}
		}
	};

	/**
	 * exchange sensor data
	 * @author fshi
	 *
	 */
	private class ExchangeData extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... voids) {
			// init the counter
			for(Device device : deviceListAdapter.getConnectableDevices()){
				mBTController.connectBTServer(device.btRawDevice, Constants.BT_CLIENT_TIMEOUT);
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
	
	/**
	 * start periodic location report
	 */
	private void startLocationReportingTask() {
		new DeviceLocationUpdateAlarm(mContext, mDeviceLocationManager);
	}

	private void stopLocationReportingTask() {
		DeviceLocationUpdateAlarm.stopReporting(mContext);
	}

	/**
	 * start update device list
	 */
	private void startDeviceListUpdateTask() {
		new DeviceListUpdateAlarm(mContext, mWebConnector);
	}

	private void stopDeviceListUpdateTask() {
		DeviceLocationUpdateAlarm.stopReporting(mContext);
	}

	
}
