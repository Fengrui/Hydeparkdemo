package info.fshi.hydeparkdemo.listener;

import info.fshi.hydeparkdemo.Device;
import info.fshi.hydeparkdemo.network.BTController;
import info.fshi.hydeparkdemo.utils.Constants;
import android.view.View;
import android.view.View.OnClickListener;

public class BTConnectButtonOnClickListener implements OnClickListener {

	Device btDevice;
	BTController btHelper;
	
	public BTConnectButtonOnClickListener(Device btDevice, BTController controller){
		this.btDevice = btDevice;
		this.btHelper = controller;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		btHelper.connectBTServer(btDevice.btRawDevice, Constants.BT_CLIENT_TIMEOUT);
	}
}