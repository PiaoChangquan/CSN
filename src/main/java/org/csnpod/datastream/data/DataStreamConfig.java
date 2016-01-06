package org.csnpod.datastream.data;

import com.google.common.base.MoreObjects;

public class DataStreamConfig {

	public static String dbName = "CSNPod";
	public static String streamName = "stream";
	public static int bufferSize = 1000;
	public static String serverIP = "117.16.146.55";
	public static int serverPort = 55555;
	public static int transferPeriod = 600;
	public static int maxTransferCntAtOnce = 5;

	public static void showDataStreamConfigValue() {
		System.out.println("DB Name: " + dbName);
		System.out.println("Stream Name: " + streamName);
		System.out.println("Buffer Size: " + bufferSize);
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server Port: " + serverPort);
		System.out.println("Transfer Period: " + transferPeriod + "s");
		System.out.println("Max Transfer Count at once: "
				+ maxTransferCntAtOnce);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("DB Name", dbName)
				.add("Stream Name", streamName).add("Buffer Size", bufferSize)
				.add("Server IP", serverIP).add("Server Port", serverPort)
				.add("Transfer Period", transferPeriod)
				.add("Max Transfer Count at once",maxTransferCntAtOnce).toString();
	}
}
