package info.fshi.hydeparkdemo.location;

import info.fshi.hydeparkdemo.data.LocationData;
import info.fshi.hydeparkdemo.network.WebServerConnector;
import info.fshi.hydeparkdemo.utils.Constants;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class DeviceLocationManager {

	private final static String TAG = "LocationManager";

	Context mContext;

	LocationManager mLocationManager;

	WebServerConnector mWebConnector;

	private double latitude = 0.0;
	private double longitude = 0.0;

	boolean gps_enabled = false;
	boolean network_enabled = false;

	LocationListener mLocationListenerGps = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.
			Log.d(TAG, "use location from GPS");
			// only report location when location changed
			if(Math.abs(location.getLatitude()- latitude) > Constants.LOCATION_UPDATE_SENSITIVITY || Math.abs(location.getLongitude()- longitude) > Constants.LOCATION_UPDATE_SENSITIVITY){
				LocationData data = new LocationData();
				data.device = BluetoothAdapter.getDefaultAdapter().getAddress();
				data.latitude = location.getLatitude();
				latitude = location.getLatitude();
				data.longitude = location.getLongitude();
				longitude = location.getLongitude();
				data.timestamp = System.currentTimeMillis();
				mLocationManager.removeUpdates(this);
				mLocationManager.removeUpdates(mLocationListenerProvider);
				mWebConnector.reportLocationData(data);
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};

	LocationListener mLocationListenerProvider = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.
			Log.d(TAG, "use location from Provider");
			if(Math.abs(location.getLatitude()- latitude) > Constants.LOCATION_UPDATE_SENSITIVITY || Math.abs(location.getLongitude()- longitude) > Constants.LOCATION_UPDATE_SENSITIVITY){
				LocationData data = new LocationData();
				data.device = BluetoothAdapter.getDefaultAdapter().getAddress();
				data.latitude = location.getLatitude();
				latitude = location.getLatitude();
				data.longitude = location.getLongitude();
				longitude = location.getLongitude();
				data.timestamp = System.currentTimeMillis();
				mLocationManager.removeUpdates(this);
				mLocationManager.removeUpdates(mLocationListenerGps);
				mWebConnector.reportLocationData(data);
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};

	public DeviceLocationManager(Context context, WebServerConnector webConnector){
		mContext = context;
		mWebConnector = webConnector;
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);		
	}

	public void requestLocationUpdates(){
		gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (!gps_enabled && !network_enabled) {
			Log.d(TAG, "no location service available");
		}
		if (gps_enabled){
			Log.d(TAG, "gps location");
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListenerGps);
		}

		if (network_enabled){
			Log.d(TAG, "provider location");
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListenerProvider);
		}

	}
}
