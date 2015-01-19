package info.fshi.hydeparkdemo;

import info.fshi.hydeparkdemo.network.BTCom;
import info.fshi.hydeparkdemo.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DeviceListAdapter extends ArrayAdapter<Device> {
	Context context; 
	int layoutResourceId;
	ArrayList<Device> deviceList = new ArrayList<Device>();

	private static final String TAG = "Device ListAdapter";

	public DeviceListAdapter(Context context, int layoutResourceId, ArrayList<Device> deviceList) {

		super(context, layoutResourceId, deviceList);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.deviceList = deviceList;
	}

	public ArrayList<Device> getDeviceList(){
		return deviceList;
	}

	/**
	 * comparator to sort device arraylist
	 * @author fshi
	 */
	public class DeviceConnStateComparator implements Comparator<Device>
	{
		@Override
		public int compare(Device lhs, Device rhs) {
			// TODO Auto-generated method stub
			return lhs.connState - rhs.connState;
		}
	}

	/** 
	 * return all devices with connectable states (state = connected or unconnected)
	 * @return
	 */
	public ArrayList<Device> getConnectableDevices(){
		ArrayList<Device> connectableDevices = new ArrayList<Device>();
		for(Device device : deviceList){

			if(device.connState == Constants.STATE_CLIENT_CONNECTED || device.connState == Constants.STATE_CLIENT_UNCONNECTED){
				connectableDevices.add(device);
			}

		}
		Log.d(TAG, "# of connectable devices " + String.valueOf(connectableDevices.size()));
		return connectableDevices;
	}

	/** 
	 * get a device using mac
	 * @param mac
	 * @return
	 */
	public Device getDevice(String mac){
		return deviceList.get(deviceIndex(mac));
	}

	/**
	 * return the index of a device, -1 if not contains
	 * @param deviceMac
	 * @return
	 */
	public int deviceIndex(String deviceMac){
		int deviceIndex = -1;
		for(int i=0; i < deviceList.size(); i++){
			if (deviceList.get(i).btRawDevice.getAddress().equalsIgnoreCase(deviceMac)){
				deviceIndex = i;
			}
		}
		return deviceIndex;
	}

	public void sortList(){
		Collections.sort(deviceList, new DeviceConnStateComparator());
	}

	/**
	 * add a device to the adapter, sort the new list then
	 * @param btDevice
	 */
	public void addDevice(Device device) {
		deviceList.add(device);
		Collections.sort(deviceList, new DeviceConnStateComparator());
	}

	public boolean canRetry(String mac){
		deviceList.get(deviceIndex(mac)).decRetryCounter();
		return deviceList.get(deviceIndex(mac)).retryCounter > 0;
	}

	/**
	 * set device message
	 * @param mac
	 * @param message
	 */
	public void setDeviceMessage(String mac, String message){
		int index = deviceIndex(mac);
		if(index >= 0){
			deviceList.get(deviceIndex(mac)).message = message;
		}
	}

	/**
	 * set device action
	 * @param mac
	 * @param action
	 */
	public void setDeviceAction(String mac, int action) {
		int state = 0;
		switch(action){
		case BTCom.BT_CLIENT_CONNECTED:
			state = Constants.STATE_CLIENT_CONNECTED;
			break;
		case BTCom.BT_SERVER_CONNECTED:
			state = Constants.STATE_SERVER_CONNECTED;
			break;
		case BTCom.BT_CLIENT_CONNECT_FAILED:
			state = Constants.STATE_CLIENT_FAILED;
			break;
		case BTCom.BT_DISCONNECTED:
			state = Constants.STATE_CLIENT_DISCONNECTED;
			break;
		case BTCom.BT_SUCCESS:
			state = Constants.STATE_CONNECT_SUCCESS;
			break;
			//		case BasicPacket.PACKET_TYPE_TIMESTAMP_ACK:
			//			state = Constants.STATE_CLIENT_CONNECTED;
			//			break;
		default:
			break;
		}
		if(state != 0){
			int index = deviceIndex(mac);
			if(index >= 0){
				deviceList.get(deviceIndex(mac)).connState = state;
				Collections.sort(deviceList, new DeviceConnStateComparator());
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DeviceHolder holder = null;

		if(row == null)
		{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DeviceHolder();

			holder.deviceMac = (TextView)row.findViewById(R.id.device_mac);
			holder.deviceName = (TextView)row.findViewById(R.id.device_name);
			holder.deviceType = (TextView)row.findViewById(R.id.device_type);
			holder.deviceRssi = (TextView)row.findViewById(R.id.device_rssi);
			holder.deviceSignal = (ImageView) row.findViewById(R.id.signal_strength);
			holder.deviceConnect = (Button) row.findViewById(R.id.device_connect);

			row.setTag(holder);
		}
		else
		{
			holder = (DeviceHolder)row.getTag();
		}

		Device device = deviceList.get(position);

		holder.deviceMac.setText(device.btRawDevice.getAddress());
		if(device.name!= null){
			holder.deviceName.setText(device.name);
		}
		if(DeviceList.devices.get(device.btRawDevice.getAddress()) == DeviceList.DEVICE_TYPE_RELAY){
			holder.deviceType.setText("R");
		} else if(DeviceList.devices.get(device.btRawDevice.getAddress()) == DeviceList.DEVICE_TYPE_SENSOR)	{
			holder.deviceType.setText("S");
		} else if(DeviceList.devices.get(device.btRawDevice.getAddress()) == DeviceList.DEVICE_TYPE_SINK){
			holder.deviceType.setText("K");
		}

		short rssi = device.rssi;
		holder.deviceRssi.setText(String.valueOf(rssi) + "dBm");
		/*
		 * -30dBm = Awesome
		 * -60dBm = Good
		 * -80dBm = OK
		 * -90dBm = Bad
		 */
		if (rssi > -30){
			holder.deviceSignal.setImageResource(R.drawable.signal_strong);
		}else if (rssi > -50){
			holder.deviceSignal.setImageResource(R.drawable.signal_high);
		}else if (rssi > -60){
			holder.deviceSignal.setImageResource(R.drawable.signal_mid);
		}else if (rssi > -80){
			holder.deviceSignal.setImageResource(R.drawable.signal_low);
		}else{
			holder.deviceSignal.setImageResource(R.drawable.signal_weak);
		}
		holder.deviceConnect.setEnabled(true);
		holder.deviceConnect.setTextColor(Color.WHITE);
		switch(device.connState){
		case Constants.STATE_CLIENT_CONNECTED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connected_button);
			holder.deviceConnect.setText(Constants.STATE_CONNECTED);
			break;
		case Constants.STATE_SERVER_CONNECTED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connected_button);
			holder.deviceConnect.setText(Constants.STATE_CONNECTED);
			break;
		case Constants.STATE_CLIENT_FAILED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connect_failed_button);
			holder.deviceConnect.setText(Constants.STATE_CONNECTION_FAILED);
			break;
		case Constants.STATE_CLIENT_DISCONNECTED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connect_failed_button);
			holder.deviceConnect.setText(Constants.STATE_DISCONNECTED);
			break;
		case Constants.STATE_CLIENT_UNCONNECTED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connect_button);
			holder.deviceConnect.setText(Constants.STATE_NOT_CONNECTED);
			break;
		case Constants.STATE_CONNECT_SUCCESS:
			holder.deviceConnect.setBackgroundResource(R.drawable.connected_button);
			holder.deviceConnect.setText(Constants.STATE_SUCCESS);
			break;
		case Constants.STATE_CLIENT_OUTDATED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connect_outdated_button);
			holder.deviceConnect.setText(Constants.STATE_NOT_CONNECTED);
			holder.deviceConnect.setTextColor(Color.BLACK);
			holder.deviceConnect.setEnabled(false);
			break;
		default:
			break;

		}
		holder.deviceConnect.setOnClickListener(device.btConnect);

		return row;
	}

	static class DeviceHolder
	{
		TextView deviceName;
		TextView deviceType;
		TextView deviceMac;
		TextView deviceRssi;
		ImageView deviceSignal;
		Button deviceConnect;
	}
}
