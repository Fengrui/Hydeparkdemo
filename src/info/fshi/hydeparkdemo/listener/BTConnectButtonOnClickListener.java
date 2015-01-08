package info.fshi.hydeparkdemo.listener;

import info.fshi.hydeparkdemo.BTDevice;
import info.fshi.hydeparkdemo.network.BTController;
import info.fshi.hydeparkdemo.utils.Constants;
import android.view.View;
import android.view.View.OnClickListener;

public class BTConnectButtonOnClickListener implements OnClickListener {

	BTDevice btDevice;
	BTController btHelper;
	
	public BTConnectButtonOnClickListener(BTDevice btDevice, BTController controller){
		this.btDevice = btDevice;
		this.btHelper = controller;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		btHelper.connectBTServer(btDevice.btRawDevice, Constants.BT_CLIENT_TIMEOUT);
	}
}