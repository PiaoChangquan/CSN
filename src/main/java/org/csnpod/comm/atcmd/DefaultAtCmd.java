package org.csnpod.comm.atcmd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.csnpod.comm.atcmd.handler.CmeErrorHandler;
import org.csnpod.comm.atcmd.handler.ResponseHandler;
import org.csnpod.comm.modem.ModemIO;
import org.csnpod.comm.modem.ResponseMode;
import org.csnpod.util.SerialConnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.pi4j.io.serial.Serial;

public class DefaultAtCmd {
	private Logger logger = LoggerFactory.getLogger(DefaultAtCmd.class); //内部使用的属性

	protected ResponseMode mode; //响应模式
	protected ResponseHandler respHdlr; //响应处理器
	protected CmeErrorHandler cmeHdlr; // 错误处理器
	protected ModemIO io;

	public DefaultAtCmd(ModemIO io, ResponseMode mode) {
		this.mode = mode;
		respHdlr = new ResponseHandler();
		cmeHdlr = new CmeErrorHandler();
		this.io = io;
	}

	public void setMode(ResponseMode mode) {
		this.mode = mode;
	}

	protected List<String> processCMD(String cmd) {
		logger.trace("Start processCMD method");

		List<String> respLines = new ArrayList<String>();
		io.sendCMD(cmd);
		Uninterruptibles.sleepUninterruptibly(20, TimeUnit.MILLISECONDS);

		String response = null;
		do {
			response = mode.equals(ResponseMode.VERBOSE) ? io.getVerboseResponse() : io.getNumericResponse();
			respLines.add(response);
//			返回模式是数字 还是 文字
		} while (!respHdlr.isRespFin(response));

		for (String resp : respLines)
			logger.debug("Response: \"{}\"", resp);

		logger.trace("End processCMD method");
		return respLines;
	}

	protected int getGeneralResult(List<String> respLines) { //得到一般结果
		logger.trace("Start getGeneralResult method");

		String finalResp = respLines.get(respLines.size() - 1);
		logger.debug("Final Response line : \"{}\"", finalResp);

		if (cmeHdlr.isCmeError(finalResp)) {
			cmeHdlr.checkCmeError(finalResp);
			return -1;
		} else {
			logger.trace("None CME Error");
		}

		if (respHdlr.isRespCode(finalResp)) {
			return respHdlr.getRespCode(finalResp);
		} else {
			logger.error("Can't find appropriate Response! Unknown Error: {}",finalResp);
			return -1;
		}
	}

	public int basicATCmd(ResponseMode mode) {
		logger.trace("Start basicATCmd method");

		logger.debug("AT CMD: AT");
		List<String> respLines = processCMD("AT");

		logger.trace("End basicATCmd method");
		return getGeneralResult(respLines);
	}
}
