package org.csnpod.sensor.sampling;

import org.csnpod.exception.SerialReadException;
import org.csnpod.util.SerialConnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.serial.Serial;

public class SensorStream {
	private Logger logger = LoggerFactory.getLogger(SensorStream.class);

	private String serialPort;
	private int baudRate;
	private Serial serial;

	public SensorStream(String serialPort, int baudRate) {
		super();
		this.serialPort = serialPort;
		this.baudRate = baudRate;
	}

	public String getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public void connectSensorStream() {
		logger.trace("Start connectSensorStream Method");

		serial = new SerialConnFactory().getSerialConnection(serialPort,baudRate);

		// Initialization
		for (int i = 0; i < 4; i++) {
			try {
				String garbageData = this.readLine();
				logger.trace("Remove garbage data: {}", garbageData);
			} catch (SerialReadException e) {
				logger.error("Can't Read(Connect) Sensor Steram");
				logger.error("Error: {}", e.toString());
			}
		}

		logger.trace("End connectSensorStream Method");
	}

	public String readLine() throws SerialReadException {
		logger.trace("Start readLine Method");

		StringBuilder sb = new StringBuilder();
		char tempChar = '-';

		while (true) {
			tempChar = serial.read();
			// logger.trace("Arrived Character: {}, ASCII Num: {}", tempChar,
			// (int) tempChar);

			if (tempChar == '\r') {
				logger.trace("Finish to receive the sensor data with \\r");
				tempChar = serial.read(); // remove '\n'
				// logger.trace("[Will be removed] Arrived Character: {}, ASCII Num: {}",
				// tempChar, (int) tempChar);
				break;
			} else {
				sb.append(tempChar);
			}

		}

		String resultData = sb.toString();
		logger.debug("Data \"{}\" will be returned", resultData);
		logger.trace("End readLine Method");
		return resultData;
	}

	public void closeSensorStream() {
		logger.trace("Start closeSensorStream Method");
		serial.close();
		logger.trace("End closeSensorStream Method");
	}
}
