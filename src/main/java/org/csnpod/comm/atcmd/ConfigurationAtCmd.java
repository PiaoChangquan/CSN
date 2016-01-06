package org.csnpod.comm.atcmd;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.csnpod.comm.modem.ModemIO;
import org.csnpod.comm.modem.ResponseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

public class ConfigurationAtCmd extends DefaultAtCmd {
	private Logger logger = LoggerFactory.getLogger(ConfigurationAtCmd.class);

	public ConfigurationAtCmd(ModemIO io, ResponseMode mode) {
		super(io, mode);
	}
	
	/**
	 * 
	 * @return  Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int rebootModem() {
		logger.trace("Start rebootModem method");
		
		logger.debug("AT CMD: AT#REBOOT");
		List<String> respLines = processCMD("AT#REBOOT");
		logger.trace("System will wait");
		Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MINUTES);
		
		logger.trace("End rebootModem method");
		return getGeneralResult(respLines);
	}
	
	/**
	 * 
	 * @return Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int setResponseMode(int val) {
		logger.trace("Start setResponseMode method");
		
		logger.debug("AT CMD: ATV{}", val);
		List<String> respLines = processCMD("ATV" + val);
//		ATV 响应格式 确定DCE响应 的格式是否带头标 是否采用数字式结果码
//		命 令		ATV0   	->DCE 发送数字式结果码
//		响 应		0    	命令有效0即OK
//		命 令 		ATV1	->DCE 发送完整的响应字符
//		响 应		OK		命令有效
		
		logger.trace("End setResponseMode method");
		return getGeneralResult(respLines);
	}

	/**
	 * 
	 * @return Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int setEchoMode(int val) {
		logger.trace("Start setEchoMode method");
		
		logger.debug("AT CMD: ATE{}", val);
		List<String> respLines = processCMD("ATE" + val);
//		确定调制解调器时候回显接收到的字符
//		命 令		ATE0		不回显
//		响 应		OK	
//		命 令		ATE1		回显
//		响 应		OK
		logger.trace("End setEchoMode method");
		return getGeneralResult(respLines);
	}

	/**
	 * 
	 * @return Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int setCMEEMode(int val) {
		logger.trace("Start setCMEEMode method");
		
		logger.debug("AT CMD: AT+CMEE={}", val);
		List<String> respLines = processCMD("AT+CMEE=" + val);
//		该命令设置是否使用结果码 +CME ERROR : <xxx> 或 +CMS ERROR :<xxx> 来代	替简单的ERROR
//		命 令	 	AT+CMEE=0 	禁止ME 错误报告使用ERROR
//		响应 		OK	
//		命 令 		AT+CMEE=1 	使用 +CME ERROR : <xxx>或 +CMS ERROR :<xxx>
//		响应 		OK
		logger.trace("End setCMEEMode method");
		return getGeneralResult(respLines);
	}
	
	/**
	 * 
	 * @return Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int setSleepMode(int val) {
		logger.trace("Start setSleepMode method");
		
		logger.debug("AT CMD: AT+CFUN={}", val);
		List<String> respLines = processCMD("AT+CFUN=" + val);
		
		logger.trace("End setSleepMode method");
		return getGeneralResult(respLines);
	}

	/**
	 * 
	 * @return 1은 신호상태 양호, 0은 신호상태 불량 혹은 단절, -1은 Error
	 */
	// TODO 파싱해야 함
	public int checkSleepMode() {
		logger.trace("Start checkSleepMode method");
		
		logger.debug("AT CMD: AT+CFUN?");
		List<String> respLines = processCMD("AT+CFUN?");
		
		logger.trace("End checkSleepMode method");
		return getGeneralResult(respLines);
	}


}
