package org.csnpod.comm.atcmd.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseHandler {
	private Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

	public boolean isRespFin(String resp) {
		logger.trace("Start isRespFin method");
		
		logger.trace("Check if the response is the final line or not");
		if (resp.matches("\\s*[0-9]\\s*")
				|| resp.matches("OK|CONNECT|NO CARRIER|ERROR|NO CARRIER0")
				|| resp.equals("") || resp.contains("+CME ERROR:") || resp.contains("SRING:")) {
			logger.debug("Response \"{}\" is the final line", resp);
			return true;
		} else {
			logger.debug("Response \"{}\" is not the final line", resp);
			return false;
		}
	}

	public boolean isRespCode(String resp) {
		logger.trace("Start isRespCode method");
		
		if (resp.length() < 2 && resp.matches("[0-8]")) {
			logger.debug("Correct Response Code: {}", resp);
			return true;
		} else {
			logger.debug("Not Corrected Response Code: {}", resp);
			return false;
		}
	}

	public int getRespCode(String resp) {
		logger.trace("Start getRespCode method");
		
		return Integer.parseInt(resp);
	}

	public String parseRespCode(int code) {
		logger.trace("Start parseRespCode method");
		
		switch (code) {
		case 0:
			return "OK";
		case 1:
			return "CONNECT";
		case 2:
			return "RING";
		case 3:
			return "NO CARRIER";
		case 4:
			return "ERROR";
		case 6:
			return "DO DIALTONE";
		case 7:
			return "BUSY";
		case 8:
			return "NO ANSWER";
		default:
			return "NO_CODE_ERROR";
		}
	}
}
