package info.fshi.hydeparkdemo.utils;

import info.fshi.hydeparkdemo.R;


public abstract class Constants {
	
	public static final boolean DEBUG = false;
	public static final int INIT_QUEUE_SIZE = 8000;
	
	public static final long WEB_LOCATION_REPORT_INTERVAL = 10 * 60 * 1000; // 10 minutes
	public static final long WEB_LOCATION_DEVICE_LIST_UPDATE_INTERVAL = 30 * 60 * 1000; // 30 minutes
	
	public static final double LOCATION_UPDATE_SENSITIVITY = 0.00001;
	
	public static final String WEB_SERVER_ADDR = "http://hydeparkdemo.herokuapp.com/";
	public static final String WEB_SERVER_TX_ADDR = "http://hydeparkdemo.herokuapp.com/tx";
	public static final String WEB_SERVER_LOC_ADDR = "http://hydeparkdemo.herokuapp.com/loc";
	public static final String WEB_SERVER_DEVICELIST_ADDR = "http://hydeparkdemo.herokuapp.com/devicelist";
	public static final String WEB_SERVER_TYPELIST_ADDR = "http://hydeparkdemo.herokuapp.com/typelist";
	
	public static final String STR_APPLICATION_NAME = "DataMule";
	public static final String TAG_APPLICATION = "DataMule";
	public static final String TAG_ACT_TEST = "ActivityTest";
	public static final String TAG_SPECIAL_TRACKER = "specialtracker";
	public static final String EXTRA_MESSENGER = "extra_messenger";
	
	// messenge bundle key
	public static final String MESSAGE_DATA_CONNECTION = "data_connection";
	public static final String MESSAGE_DATA_TIMESTAMP = "data_timestamp";
	public static final String MESSAGE_DATA_DEVICE_MAC = "data_device_mac";
	public static final String MESSAGE_DATA_DEVICE_NAME = "data_device_name";
	public static final String MESSAGE_ACK_DATA_TIMESTAMP = "ack_data_timestamp";
	public static final String MESSAGE_DATA = "message_data";
	
	// Device Info Dialog
	public static final String TITLE_DEVICE_INFO_DIALOG = "DEVICE INFO";
	
	// BT meta
	public static final int BT_CLIENT_TIMEOUT = 3000;
	public static final int BT_CONN_MAX_RETRY = 3;
	public static final int BT_RETRY_BACK_OFF = 30000;
	
	// BT activity intent extra name
	public static final String INTENT_EXTRA_DEVICE_MAC = "intent_extra_device_mac";
	
	// BT client and server state in listview, start from 200
	public static final int STATE_CLIENT_CONNECTED = 201;
	public static final int STATE_SERVER_CONNECTED = 2010;
	public static final int STATE_CLIENT_UNCONNECTED = 202;
	public static final int STATE_CLIENT_FAILED = 203;
	public static final int STATE_CLIENT_DISCONNECTED = 205;
	public static final int STATE_CLIENT_OUTDATED = 206;
	public static final int STATE_CONNECT_SUCCESS = 207;
	
	
	// String, upper case
	public static final String STATE_CONNECTED = "CONNECTED";
	public static final String STATE_CONNECTION_FAILED = "FAILED";
	public static final String STATE_NOT_CONNECTED = "CONNECT";
	public static final String STATE_DISCONNECTED = "DISCONN";
	public static final String STATE_SUCCESS = "SUCCESS";
	
	public static final String LOG_TIMESTAMP_CONNECT = "log_timestamp_connect";
	public static final String LOG_TIMESTAMP_WRITE = "log_timestamp_write";
	public static final String LOG_TIMESTAMP_WRITE_FINISHED = "log_timestamp_write_finished";
	public static final String LOG_TIMESTAMP_ACK_RECEIVED = "log_timestamp_ack_received";
	
	// BT client and server status, start from 10
	public static final int SERVER_DISCONNECTED = 10;
	public static final int SERVER_CONNECTED = 11;
	public static final int CLIENT_DISCONNECTED = 12;
	public static final int CLIENT_CONNECTED = 13;
	
	// Shared preference keys
	public static final String SP_RADIO_SCAN_DURATION_ID = "sp_radio_bt_scan_duration_id";
	public static final String SP_RADIO_SCAN_DURATION = "sp_radio_bt_scan_duration";
	public static final String SP_RADIO_SCAN_INTERVAL_ID = "sp_radio_bt_scan_interval_id";
	public static final String SP_RADIO_SCAN_INTERVAL = "sp_radio_bt_scan_interval";
	public static final String SP_CHECKBOX_BT_AUTO_SCAN = "sp_checkbox_bt_auto_scan";
	public static final String SP_BT_OPERATION_STATE = "sp_bt_op_state";
	// default values
	public static final int DEFAULT_BT_SCAN_INTERVAL = 300*1000;
	public static final int DEFAULT_BT_SCAN_DURATION = 10000;
	public static final int DEFAULT_BT_SCAN_INTERVAL_ID = R.id.radio_scan_interval_5;
	public static final int DEFAULT_BT_SCAN_DURATION_ID = R.id.radio_scan_duration_10;
	
	
	// bt settings ID
	public static final int BT_SCAN_DURATION_SETUP_ID = 400;
	public static final int BT_SCAN_INTERVAL_SETUP_ID = 401;

	
}
