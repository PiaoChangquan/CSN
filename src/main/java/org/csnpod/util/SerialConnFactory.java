package org.csnpod.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class SerialConnFactory {
	private Logger logger = LoggerFactory.getLogger(SerialConnFactory.class);
	private Serial serial;
	
	public Serial getSerialConnection(String port, int baudRate) {
		serial = SerialFactory.createInstance();
		serial.open(port, baudRate);
		
		return serial;
	}
	
	public List<String> findComPort(String portOpt) {
		logger.trace("Start findComPort Method");
		
		logger.debug("Find \"{}\" Modem Port", portOpt);
		String line = null;
		List<String> usbPorts = new ArrayList<String>();
		try {
			Process process = Runtime.getRuntime().exec("ls /dev");
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			while ((line = stdIn.readLine()) != null) {
				if (line.contains(portOpt)) {
					String newPort = "/dev/"+line;
					logger.debug("Find \"{}\" port", newPort);
					
					usbPorts.add(newPort);
					logger.debug("Currenlty Added ports: {}", usbPorts);
				}
			}

		} catch (IOException e) {
			logger.error("Can't get the list of \"{}\" port", portOpt);
			logger.error("Error: {}", e.toString());
		}
		
		logger.info("Current Modem Ports: {}", usbPorts);
		
		logger.trace("End findComPort Method");
		return usbPorts;
	}
}
