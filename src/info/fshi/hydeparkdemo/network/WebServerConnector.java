package info.fshi.hydeparkdemo.network;

import info.fshi.hydeparkdemo.DeviceList;
import info.fshi.hydeparkdemo.data.LocationData;
import info.fshi.hydeparkdemo.data.TransactionData;
import info.fshi.hydeparkdemo.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class WebServerConnector {

	private final static String TAG = "web server connector";
	
	public WebServerConnector(){
		
	}
	
	private class SendTransactionRecordTask extends AsyncTask<TransactionData, Void, Void> {
		protected Void doInBackground(TransactionData... data) {
			// init the counter
			TransactionData txData = data[0];
			HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(Constants.WEB_SERVER_TX_ADDR);
		    try {
		        // Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("sender", txData.senderAddr));
		        nameValuePairs.add(new BasicNameValuePair("receiver", txData.receiverAddr));
		        nameValuePairs.add(new BasicNameValuePair("size", String.valueOf(txData.packetSize)));
		        nameValuePairs.add(new BasicNameValuePair("cost", String.valueOf(txData.cost)));
		        nameValuePairs.add(new BasicNameValuePair("timestamp", String.valueOf(txData.timestamp)));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		        // Execute HTTP Post Request
		        //HttpResponse response = 
		        httpclient.execute(httppost);

		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    } finally {
		    	httppost.abort();
		    }
			return null;
		}
	}
	
	public void reportTransactionData(TransactionData data) {
	    // Create a new HttpClient and Post Header
		new SendTransactionRecordTask().execute(data);
	}
	
	private class SendLocationRecordTask extends AsyncTask<LocationData, Void, Void> {
		protected Void doInBackground(LocationData... data) {
			// init the counter
			LocationData locData = data[0];
			HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(Constants.WEB_SERVER_LOC_ADDR);
		    try {
		        // Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("device", locData.device));
		        nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(locData.latitude)));
		        nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(locData.longitude)));
		        nameValuePairs.add(new BasicNameValuePair("timestamp", String.valueOf(locData.timestamp)));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		        // Execute HTTP Post Request
		        //HttpResponse response = 
		        httpclient.execute(httppost);
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    } finally {
		    	httppost.abort();
		    }
			return null;
		}
	}
	
	public void reportLocationData(LocationData data){
		new SendLocationRecordTask().execute(data);
	}
	
	private class UpdateDeviceList extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... Void) {
			// init the counter
			// get type list first
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet requestTypeList = new HttpGet(Constants.WEB_SERVER_TYPELIST_ADDR);
		    try {
		        
		    	HttpResponse response = httpclient.execute(requestTypeList);
		    	JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
		    	Iterator<String> keysIterator = json.keys();
		    	while (keysIterator.hasNext())
		    	{
		    	        String id = (String)keysIterator.next(); // type key
		    	        String name = json.getString(id);
		    	        if(name.equalsIgnoreCase("sensor")){
		    	        	DeviceList.DEVICE_TYPE_SENSOR = Integer.parseInt(id);
		    	        }else if(name.equalsIgnoreCase("relay")){
		    	        	DeviceList.DEVICE_TYPE_RELAY = Integer.parseInt(id);
		    	        }else if(name.equalsIgnoreCase("sink")){
		    	        	DeviceList.DEVICE_TYPE_SINK = Integer.parseInt(id);
		    	        }
		    	        Log.d(TAG, name + ":" + id);
		    	}

		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				requestTypeList.abort();
			}
		    // then get device list
		    HttpGet requestDeviceList = new HttpGet(Constants.WEB_SERVER_DEVICELIST_ADDR);
		    try {
		    	HttpResponse response = httpclient.execute(requestDeviceList);
		    	JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
		    	Iterator<String> keysIterator = json.keys();
		    	while (keysIterator.hasNext()) 
		    	{
		    	        String mac = (String)keysIterator.next();
		    	        String type = json.getString(mac);
		    	        DeviceList.devices.put(mac, Integer.parseInt(type));
		    	        Log.d(TAG, mac + ":" + type);
		    	}
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    } catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				requestTypeList.abort();
			}
			return null;
		}
	}
	
	public void updateDeviceList(){
		new UpdateDeviceList().execute();
	}
	
}
