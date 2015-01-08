package info.fshi.hydeparkdemo.packet;

public abstract class BasicPacket {
	// BT messge header
	// format:  type|data
	public static final String PACKET_TYPE = "type";
	public static final String PACKET_DATA = "data";
	
	// data type identifier
	public static final int PACKET_TYPE_SENSOR_REQUEST = 100;
	public static final int PACKET_TYPE_SENSOR_READING = 101;
	public static final int PACKET_TYPE_SENSOR_READING_ACK = 102;
	
}
