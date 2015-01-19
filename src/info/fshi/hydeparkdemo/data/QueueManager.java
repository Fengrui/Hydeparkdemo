package info.fshi.hydeparkdemo.data;

import info.fshi.hydeparkdemo.utils.Constants;

import java.util.Random;

import android.util.Log;

public class QueueManager {

	private StringBuffer sb = new StringBuffer();
	private final static String TAG = "QueueManager";
	
	public QueueManager(){
		// for test purpose, init queue content
		if(Constants.DEBUG){
			initQueue();
			Log.d(TAG, "init queue length to " + sb.length());
		}
	}

	private void initQueue(){
		Random rand = new Random();
		for(int i=0; i<rand.nextInt(80000) + 800000; i++){
			sb.append("a");
		}
	}
	
	public void appendToQueue(String textToAppend){
		sb.append(textToAppend);
	}

	public String getFromQueue(int length){
		String newSb = sb.substring(length);
		String stringToReturn = sb.substring(0, length);
		sb = new StringBuffer(newSb);
		return stringToReturn;

	}

	public String getFromQueue(){
		String stringToReturn = sb.toString();
		sb = new StringBuffer();
		if(Constants.DEBUG){
			initQueue();
		}
		return stringToReturn;
	}

	public int getQueueLength(){
		Log.d(TAG, "string size in byte " + String.valueOf(String.valueOf(sb).getBytes().length));
		return sb.length();
	}
}
