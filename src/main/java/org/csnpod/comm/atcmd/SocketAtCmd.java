package org.csnpod.comm.atcmd;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.csnpod.comm.modem.ModemIO;
import org.csnpod.comm.modem.ResponseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

public class SocketAtCmd extends DefaultAtCmd {
	private Logger logger = LoggerFactory.getLogger(SocketAtCmd.class);

	public SocketAtCmd(ModemIO io, ResponseMode mode) {
		super(io, mode);
	}

	private char processSendCMD(String cmd, String data) {
		logger.trace("Start processSendCMD method");
		io.sendCMD(cmd);
		Uninterruptibles.sleepUninterruptibly(20, TimeUnit.MILLISECONDS);
		char ret = io.getSocketSendResponse();

		logger.trace("End processSendCMD method");
		return ret;
	}

	/**
	 * 
	 * @return Socket 준비 완료되었을 경우 할당된 IP주소를 반환, 그렇지 않으면 null 반환
	 * @throws NetworkContextException
	 */
	public String prepareSock() {
		logger.trace("Start prepareSock method");

		logger.debug("AT CMD: AT#SGACT=1,1");

		List<String> respLines = processCMD("AT#SGACT=1,1");		
//		DATA 호 Open
//		AT#SGACT=1,1
//		#SGACT: 123.229.164.94 // Modem에게 부여된 IP Address
//		OK
		String finalResp = respLines.get(respLines.size() - 1);

		if (cmeHdlr.checkCmeError(finalResp) == -1) {
			logger.trace("None CME Error");
		} else {
			return null;
		}

		String ip = "NOT_ASSIGNED_ADDR";

		if (respHdlr.isRespCode(finalResp)) {

			for (String resp : respLines) {
				logger.debug("Temporary response line: \"{}\"", resp);
				if (resp.contains("#SGACT:")) {
					logger.trace("Parsing Assigned IP addr");
					ip = resp.substring(8);
					logger.debug("Parsed IP Addr \"{}\" within #SGACT", ip);
				}
			}

			logger.debug("Received IP ADDR: {}", ip);
			return ip;
		} else {
			logger.error("Can't find appropriate Response! Unknown Error: {}",finalResp);
			return null;
		}
	}

	/**
	 * 
	 * @return Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int freeSock() {
		logger.trace("Start freeSock method");

		logger.debug("AT CMD: AT#SGACT=1,0");
		List<String> respLines = processCMD("AT#SGACT=1,0");
//		DATA 호 OPEN
//		AT#SGACT=1,1
//		#SGACT: 123.229.164.94 // Modem에게 부여된 IP Address
//		OK
		logger.trace("End freeSock method");
		return getGeneralResult(respLines);
	}

	/**
	 * 
	 * @return Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int connSock(String addr, int port) {
		logger.trace("Start connSock method");

		logger.debug("AT CMD: AT#SD=1,0," + port + ",\"" + addr + "\",0,0,1");
		List<String> respLines = processCMD("AT#SD=1,0," + port + ",\"" + addr + "\",0,0,1");
//		Test Server에 접속
//		AT#SD=1,0,10510,"88.37.127.164",0,0,1
//		OK
		logger.trace("End connSock method");
		return getGeneralResult(respLines);
	}

	/**
	 * 
	 * @return Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int closeSock() {
		logger.trace("Start closeSock method");

		logger.debug("AT CMD: AT#SH=1");

		List<String> respLines = processCMD("AT#SH=1");
//		Server와의 접속 해제 하기
//		AT#SH=1
//		OK
		logger.trace("EndcloseSock method");
		return getGeneralResult(respLines);
	}

	/**
	 * 
	 * @param data
	 * @return Modem의 기본 Response 코드 준수(0은 OK), -1은 Error
	 */
	public int sendData(String data) {
		logger.trace("Start sendData method");

		logger.debug("AT CMD: AT#SSEND=1");

		if (processSendCMD("AT#SSEND=1", data) == '>') {
//			Data 보내기
//			AT#SSEND=1
//			> Hello, Server[Ctrl+Z] // Ctrl +Z을 눌러야 데이터가 보내짐
//			OK
//			
			
			logger.trace("Come Waiting OK MSG");
			logger.debug("data \"{}\" will be sended", data);
			List<String> respLines = processCMD(data + "\r\u001A");
			return getGeneralResult(respLines);
		} else
			return -1;
	}

	/**
	 * 
	 * @param byteNum
	 * @return 정상적으로 데이터 수신하면 값을, 그렇지 않을 경우 null 반환
	 */
	public String readData(int byteNum) {
		logger.trace("Start readData method");

		logger.trace("Waiting For the Data Receive MSG");
		int ret = io.getSocketRcvResponse();
		byteNum += 2;
		if (ret > 0) {
			logger.debug("AT CMD: AT#SRECV=1," + byteNum);
			List<String> respLines = processCMD("AT#SRECV=1," + byteNum);
//			Data 읽기
//			AT#SRECV=1,100 // 읽고자 하는 Data 양을 100 byte로 설정
//			#SRECV: 1,13 // 실질적으로 buffer에 저장된 Data는 13 byte
//			Hello, Server // 읽혀진 Data
//			OK
			int i = 0;
			for (i = 0; i < respLines.size(); i++) {
				String resp = respLines.get(i);
				logger.debug("Temporary response line: \"{}\"", resp);
				if (resp.contains("#SRECV:")) {
//					Command Mode Server Test
//					1. DATA 호 OPEN
//					AT#SGACT=1,1
//					#SGACT: 123.229.164.94 // Modem에게 부여된 IP Address
//					OK
					logger.trace("Data #SRECV Check!");
					i++;
					break;
				}
			}
			String retData = respLines.get(i);
			logger.debug("This Data  \"{}\" will be returned", retData);
			return retData;
		} else {
			return null;
		}
	}

}
