package org.csnpod.datastream;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.csnpod.comm.atcmd.SocketAtCmd;
import org.csnpod.comm.data.CommConfig;
import org.csnpod.comm.socket.SocketConnFactory;
import org.csnpod.comm.socket.SocketConnector;
import org.csnpod.datastream.data.DataStreamConfig;
import org.csnpod.mgnt.data.CsnPodConfig;
import org.csnpod.sensor.data.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;

public class DataStreamControllerUnit extends Thread {
	private static Logger logger = LoggerFactory
			.getLogger(DataStreamControllerUnit.class);

	protected BlockingQueue<SensorData> sensorDataQueue;
	private List<SensorData> bufferedQueue;
	private StreamManager streamManager;
	private SocketConnector socket;
	private ObjectMapper mapper;

	private boolean abort = false; // Flag in case of abort

	public DataStreamControllerUnit(BlockingQueue<SensorData> queue, SocketAtCmd sockAtCmd) {
		super("Data Stream Controller Unit");
		this.sensorDataQueue = queue;
		streamManager = new StreamManager();
		streamManager.setUpStreamQueue();
		bufferedQueue = streamManager.getUntransferredDataQueue();

//		socket = new SocketConnFactory().getCellularConnector(sockAtCmd);
		socket = new SocketConnFactory().getEthernetConnector();
		mapper = new ObjectMapper();
	}

	@Override
	public void run() {
		logger.trace("Start run method");

		while (true) {
			logger.info("Wait {}s for the next transferring time",
					DataStreamConfig.transferPeriod);
			Uninterruptibles.sleepUninterruptibly(DataStreamConfig.transferPeriod, TimeUnit.SECONDS);

			synchronized (this) {
				if (abort) {
					sendBufferedSensorData();
					break;
				}
			}

			if (sensorDataQueue.peek() != null) {
				try {
					SensorData data = sensorDataQueue.take();
					logger.info("Poped Sensor Data: {}", data);
					streamManager.insertData(data);
					bufferedQueue.add(data);
					logger.debug("Finish to put sensor data into buffered queue(size: {})",bufferedQueue.size());
				} catch (InterruptedException e) {
					logger.error("Can't put senssor data into buffered queue");
					logger.error("Error: {}", e.toString());
				}

			}

			int maxTransferCnt = 0;
			while (bufferedQueue.size() > DataStreamConfig.bufferSize&& maxTransferCnt < DataStreamConfig.maxTransferCntAtOnce) {
				sendBufferedSensorData();
				logger.debug("Remained BufferedQueue Number: {}",bufferedQueue.size());
				maxTransferCnt++;
			}
		}

		logger.trace("End run method");
	}

	private void sendBufferedSensorData() {
		int len = bufferedQueue.size() > DataStreamConfig.bufferSize ? DataStreamConfig.bufferSize
				: bufferedQueue.size();
		
		List<SensorData> dataList = new LinkedList<SensorData>(
				bufferedQueue.subList(0, len));
		bufferedQueue.subList(0, len).clear();

		String jsonData = "";
		try {
			jsonData = mapper.writeValueAsString(dataList);
		} catch (JsonProcessingException e) {
			logger.error("Can't translate sensor data into json");
			logger.error("Error: {}", e.toString());
		}
		logger.debug("Translated json data \"{}\"", jsonData);

		int ret = socket.connect(DataStreamConfig.serverIP,DataStreamConfig.serverPort);

		Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);

		if (ret != 0)
			ret = socket.connect(DataStreamConfig.serverIP,
					DataStreamConfig.serverPort);
		logger.debug("Socket Connect Return: {}", ret);

		Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);

		if (ret != 0)
			ret = socket.connect(DataStreamConfig.serverIP,
					DataStreamConfig.serverPort);
		logger.debug("Socket Connect Return: {}", ret);

		socket.write(jsonData);
		socket.close();

		streamManager.addCurPtr(len);
	}

	public void abort() {
		this.abort = true;
	}
}
