package org.csnpod.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.csnpod.sensor.data.PhysicalSensorInformation;
import org.csnpod.sensor.data.SensorData;
import org.csnpod.sensor.data.SensorUnitConfig;
import org.csnpod.util.SerialConnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorControllerUnit extends Thread {
	private Logger logger = LoggerFactory.getLogger(SensorControllerUnit.class);

	protected BlockingQueue<SensorData> sensorDataQueue;
	private List<Sensor> sensorThreadList;
	private boolean abort = false; // Flag in case of abort
	
	public SensorControllerUnit(BlockingQueue<SensorData> queue) {
		super("Sensor Controller Unit");
		this.sensorDataQueue = queue;
	}
	
	@Override
	public void run() {
		logger.trace("Start run Method");
		
		logger.info("Init Sensors");
		initSensorThreads();
		logger.info("{}s Sensor has been created", sensorThreadList.size());

		while (true) {
			synchronized (this) {
				if (abort) {
					logger.info("Abort Sensor Controller Unit");
					stopSensorThreads();
					logger.info("Finish stopping the Sensor");
					break;
				}
			}
		}
		
		logger.trace("End run Method");
	}

	private void initSensorThreads() {
		logger.trace("Start initSensorThreads Method");
		
		int i = 0;
		sensorThreadList = new ArrayList<Sensor>();
		List<String> comPorts = new SerialConnFactory().findComPort("ACM");
		logger.debug("{} ports are found", comPorts.size());
		logger.debug("Port Data: {} ", comPorts);
		
		if(comPorts.size() != SensorUnitConfig.snsrCount)
			logger.error("Can't match the sensor port and sensor");
		
		logger.trace("Creating Sensor");
		for (PhysicalSensorInformation metadata : SensorUnitConfig.physicalSnsrInfo) {
			logger.debug("Create \"{}\" Sensor", metadata.getName());
			logger.debug("Select Port \"{}\"", comPorts.get(i));
			 
			Sensor sensorThread = new Sensor(metadata.getName(),comPorts.get(i++), 9600, sensorDataQueue, metadata);
			logger.trace("Start \"{}\"", sensorThread.getName());
			sensorThread.start();
			
			sensorThreadList.add(sensorThread);
			logger.debug("Created Sensor: {}", sensorThreadList);
			logger.debug("Total Sensor number: {}", sensorThreadList.size());
		}
		
		logger.trace("End initSensorThreads Method");
	}

	private void stopSensorThreads() {
		logger.trace("Start stopSensorThreads Method");
		
		if (sensorThreadList != null) {
			logger.trace("Kill Each Sensor");
			
			for (Sensor sensor : sensorThreadList) {
				String sensorThreadName = sensor.getName();
				logger.debug("\"{}\" Sensor will be killed", sensorThreadName);
				sensor.abort();
				
				try {
					sensor.join();
					logger.trace("Finish to Kill \"{}\"", sensorThreadName);
				} catch (InterruptedException e) {
					logger.error("Can't kill the \"{}\" Sensor", sensorThreadName);
					logger.error("Error: {}", e.toString());
				}
				
			}
		}
		
		logger.trace("End stopSensorThreads Method");
	}

	public void abort() {
		this.abort = true;
	}
}
