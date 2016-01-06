package org.csnpod.sensor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.csnpod.exception.SerialReadException;
import org.csnpod.sensor.data.LogicalSensorMetadata;
import org.csnpod.sensor.data.SensorData;
import org.csnpod.sensor.data.PhysicalSensorInformation;
import org.csnpod.sensor.parser.SensorDataParser;
import org.csnpod.sensor.sampling.SensorStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

public class Sensor extends Thread {
	private Logger logger = LoggerFactory.getLogger(Sensor.class);
	protected BlockingQueue<SensorData> sensorDataQueue;
	private SensorStream stream;
	private PhysicalSensorInformation physicalInfo;
	private SensorDataParser parser;

	private boolean abort = false; // Flag in case of abort

	public Sensor(String name, String comPort, int baudRate,
			BlockingQueue<SensorData> sensorDataQueue,
			PhysicalSensorInformation metadata) {
		super(name);
		stream = new SensorStream(comPort, baudRate);
		this.sensorDataQueue = sensorDataQueue;
		this.physicalInfo = metadata;
		parser = new SensorDataParser(physicalInfo.getParsingRegex(),
				physicalInfo.getTargetInfo());
	}

	@Override
	public void run() {
		logger.trace("Start run Method");
		
		logger.info("Start \"{}\"", physicalInfo.getName());

		logger.info("Connect to Sensor Stream");
		stream.connectSensorStream();

		while (true) {
			logger.info("Wait {}s for the next sampling time",physicalInfo.getPeriod());
			Uninterruptibles.sleepUninterruptibly(physicalInfo.getPeriod(),TimeUnit.SECONDS);

			synchronized (this) {
				if (abort) {
					logger.info("Close \"{}\"", physicalInfo.getName());

					break;
				}
			}

			logger.trace("Reading Data from Sensor...");
			try {
				String rawData = stream.readLine();
				logger.info("Data from sensor: {}", rawData);

				logger.trace("Parsing Sensor Data");
				Map<String, Object> parseResult = parser.parseData(rawData);

				if (parseResult == null) {
					logger.warn("Sensor Data Error");
					continue;
				}

				for (Object localIdKey : parseResult.keySet()) {
					logger.debug("Parsed Result: {}",parseResult.get(localIdKey));

					LogicalSensorMetadata sensorMeta = null;
					for (LogicalSensorMetadata tempSensorMeta : physicalInfo.getSensors()) {
						if (localIdKey.equals(tempSensorMeta.getLocalId())) {
							sensorMeta = tempSensorMeta;
						}
					}

					Date date = Calendar.getInstance().getTime();
					String currentTimestamp = new SimpleDateFormat(sensorMeta.getTimeFormat()).format(date);
					logger.debug("Making Timestamp: {}", currentTimestamp);

					SensorData sensorData = new SensorData(sensorMeta.getCsnId(), currentTimestamp,
							(String) parseResult.get(localIdKey));
					logger.info("Created Data: {}", sensorData);

					logger.trace("Send Data to Data Stream Controller");
					sensorDataQueue.put(sensorData);
					logger.info("Finish to put Data into Data Stream Controller");
				}

			} catch (SerialReadException e) {
				logger.error("Can't read stream from sensor");
				logger.error("Error: {}", e.toString());
			} catch (InterruptedException e) {
				logger.error("Can't send Sensor Data to Data stream controller unit");
				logger.error("Error: {}", e.toString());
			}

		}

		stream.closeSensorStream();
		logger.info("\"{}\" will be Stopped", physicalInfo.getName());

		logger.trace("End run Method");
	}

	public void abort() {
		this.abort = true;
	}

}
