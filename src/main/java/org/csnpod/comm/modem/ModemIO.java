package org.csnpod.comm.modem;

import java.util.List;

import org.csnpod.util.SerialConnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.serial.Serial;

public class ModemIO {
	private Logger logger = LoggerFactory.getLogger(ModemIO.class);
	private Serial serial;

	public void openIO() {
		logger.trace("Start connSerial method");
		
		List<String> usbPorts = new SerialConnFactory().findComPort("USB");
		logger.debug("Select Port {}", usbPorts.get(2));
		serial = new SerialConnFactory().getSerialConnection(usbPorts.get(2),115200);
		
		logger.trace("End connSerial method");
	}

	public void closeIO() {
		logger.trace("Start closeSerial method");
		serial.close();
		logger.trace("End connSerial method");
	}

	public void sendCMD(String cmd) {
		serial.writeln(cmd);
	}

	public String getVerboseResponse() {
		logger.trace("Start getVerboseResponse method");
//		得到文字是的回复
		StringBuilder sb = new StringBuilder();
		char tempChar = '\0';
		boolean msgStartFlag = false;

		while (true) {
			tempChar = serial.read();

			if (tempChar == '\r' && !msgStartFlag) {
				logger.trace("Start to receive the response with \\r");
				tempChar = serial.read();
				msgStartFlag = true;
			} else if (tempChar == '\r' && msgStartFlag) {
//				为了取第二个值 第一个if 到第一个 \r 将 msgStartFlag 变为ture 第二个if 输出 Serial.read();
				logger.trace("Finish to receive the response with \\r");
				tempChar = serial.read();
				logger.debug("Assembled Response: {}", sb.toString());
				return sb.toString();
			} else if (tempChar == (char) 65535) {
				logger.warn("Modem doesn't responded correctly");
				return "";
			} else {
				sb.append(tempChar);
			}
		}
	}

	public String getNumericResponse() {
//			得到数字的回复
		logger.trace("Start getNumericResponse method");

		StringBuilder sb = new StringBuilder();
		char tempChar = '\0';

		while (true) {
			tempChar = serial.read();

			if (tempChar == '\r') {
				logger.trace("Finish to receive the response with \\r");
				String response = sb.toString();
				if (response.matches("\\D.*")) {
					logger.debug("The response line \"{}\" is text data type",response);
					tempChar = serial.read();
				}

				logger.debug("Assembled Response: {}", response);
				return response;
			} else if (tempChar == (char) 65535) {
				logger.warn("Modem doesn't responded correctly");
				return "";
			} else if (tempChar == (char) 26) {
				logger.trace("Remove unnecessary character in Data Send Mode");
				continue;
			} else {
				sb.append(tempChar);
			}
		}
	}

	public char getSocketSendResponse() {
		logger.trace("Start getSocketSendResponse method");

		logger.trace("Waiting for Data Send Command \">\"");
		StringBuilder sb = new StringBuilder();
		char tempChar = '\0';

		while (true) {
			tempChar = serial.read();
			// logger.trace("Arrived Character: {}, ASCII Num: {}", tempChar,
			// (int) tempChar);

			if (tempChar == '>') {
				tempChar = serial.read();
				// logger.trace("[Will be removed] Arrived Character: {}, ASCII Num: {}",
				// tempChar, (int) tempChar);
				logger.trace("Ready to send data");
				return '>';
			} else if (tempChar == (char) 65535) {
				logger.warn("Modem doesn't responded correctly");
				return '\0';
			} else {
				sb.append(tempChar);
			}
		}
	}

	public int getSocketRcvResponse() {
		logger.trace("Start getSocketRcvResponse method");

		logger.trace("Waiting for Data Arriving Command \"SRING\"");
		StringBuilder sb = new StringBuilder();
		char tempChar = '\0';
		int count = 0;

		while (true) {
			tempChar = serial.read();

			if (tempChar == (char) 65535) {
				logger.warn("Modem doesn't responded correctly");
				logger.debug("Waiting Count: {}", count++);
			} else {
				// logger.trace("Arrived Character: {}, ASCII Num: {}",
				// tempChar, (int) tempChar);
				sb.append(tempChar);
			}

			if (sb.toString().contains("SRING:")) {
//				Data 수신 Notification
//				SRING: 1 // Server로 부터 Incoming Data를 받은 경우 , SRING noti가 발생
//						// Data는 현재 , 수신 버퍼에 저장이 된 상태
				
				logger.trace("\"SRING\" Come");
				while (true) { // remove additional characters ...
					tempChar = serial.read();
					// logger.trace("[Will be removed] Arrived Character: {}, ASCII Num: {}",tempChar,
					// (int) tempChar);
					if (tempChar == '\r')
						break;
				}
				return 1;
			}

			if (count > 10) {
				logger.trace("Data Arriving Command \"SRING\" non coming");
				return 0;
			}
		}
	}
}
