package org.csnpod.comm.atcmd.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmeErrorHandler {
	private Logger logger = LoggerFactory.getLogger(CmeErrorHandler.class);
	private int cmeErrNo;

	public boolean isCmeError(String resp) {
		logger.trace("Start isCmeError method");
		
		if (resp.contains("+CME ERROR:")) {
			logger.trace("This response has CME ERROR");
			return true;
		} else {
			logger.trace("This response doesn't have CME ERROR");
			return false;
		}
	}

	private int parseCmeErrorNo(String resp) {
		logger.trace("Start parseCmeErrorNo method");
		
		logger.trace("Parse CME ERROR Number");
		cmeErrNo = Integer.parseInt(resp.substring(12));
		logger.debug("The response has CME Error: {}", cmeErrNo);
		
		logger.trace("End parseCmeErrorNo method");
		return cmeErrNo;
	}

	/**
	 * 
	 * @param resp
	 * @return CME Error일 경우 errNo 반환, 그렇지 않으면 -1 반환 
	 */
	public int checkCmeError(String resp) {
		logger.trace("Start checkCmeError method");
		
		if (isCmeError(resp)) {
			cmeErrNo = parseCmeErrorNo(resp);
			logger.error("CME Error Check Result: {}", cmeErrNo);
			// TODO Error Logging 또는 Report 처리
			return cmeErrNo;
		} else {
			logger.trace("None CME Error");
			return -1;
		}
	}

}
