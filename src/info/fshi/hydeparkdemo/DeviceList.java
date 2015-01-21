package info.fshi.hydeparkdemo;

import java.util.HashMap;
import java.util.Map;

public class DeviceList {
	
	public static int DEVICE_TYPE_SENSOR = 1;
	public static int DEVICE_TYPE_RELAY = 2;
	public static int DEVICE_TYPE_SINK = 3;
	
    public static Map<String, Integer> devices = new HashMap<String, Integer>(); // mac:type
    
    public static void init(){
    	devices = new HashMap<String, Integer>();
    }
}
