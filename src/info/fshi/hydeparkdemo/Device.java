package info.fshi.hydeparkdemo;

import info.fshi.hydeparkdemo.utils.Constants;
import android.bluetooth.BluetoothDevice;
import android.view.View.OnClickListener;

public class Device {
	short rssi;
	String name;
	int connState;
	public BluetoothDevice btRawDevice;
	OnClickListener btConnect;
	int retryCounter;
	String message = null;
	
	private final static String TAG = "Device";
	
	/**
	 * init a self-defined bluetooth device using android bluetooth object
	 * @param device
	 */
	public Device(BluetoothDevice device){
		this.btRawDevice = device;
		this.retryCounter = Constants.BT_CONN_MAX_RETRY;
	}
	
	public void decRetryCounter(){
		this.retryCounter--;
	}
		
	public void resetRetryCounter(){
		this.retryCounter = Constants.BT_CONN_MAX_RETRY;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
}
