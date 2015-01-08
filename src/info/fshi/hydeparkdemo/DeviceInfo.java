package info.fshi.hydeparkdemo;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

public class DeviceInfo extends Dialog{

	public DeviceInfo(Context context, Device device) {
		super(context);
		// TODO Auto-generated constructor stub
		setContentView(R.layout.activity_device_info);
		TextView name = (TextView) findViewById(R.id.device_info_name);
		TextView mac = (TextView) findViewById(R.id.device_info_mac);
		TextView rssi = (TextView) findViewById(R.id.device_info_rssi);
		TextView message = (TextView) findViewById(R.id.device_info_message);
		if(name != null)
			name.setText(device.btDevice.name);
		mac.setText(device.btDevice.btRawDevice.getAddress());
		rssi.setText(String.valueOf(device.btDevice.rssi) + "dBm");
		if(message != null)
			message.setText(device.btDevice.message);
	}

}
