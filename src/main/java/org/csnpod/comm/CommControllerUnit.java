package org.csnpod.comm;

import org.csnpod.comm.atcmd.CommStateAtCmd;
import org.csnpod.comm.atcmd.ConfigurationAtCmd;
import org.csnpod.comm.atcmd.DefaultAtCmd;
import org.csnpod.comm.atcmd.SocketAtCmd;
import org.csnpod.comm.data.CommConfig;
import org.csnpod.comm.data.CommState;
import org.csnpod.comm.modem.ModemIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommControllerUnit {
	private Logger logger = LoggerFactory.getLogger(CommStateAtCmd.class);

	public static CommState commState = CommState.DEFAULT;
	private ModemIO io;
	private DefaultAtCmd defaultAtCmd;
	private ConfigurationAtCmd confAtCmd;
	private CommStateAtCmd commStateAtCmd;
	private SocketAtCmd sockAtCmd;
	
//	private boolean abort = false; // Flag in case of abort

	public CommControllerUnit() {
		//super("Communication Controller Unit");
		io = new ModemIO();
		io.openIO();
		
		defaultAtCmd = new DefaultAtCmd(io, CommConfig.respMode);
		confAtCmd = new ConfigurationAtCmd(io, CommConfig.respMode);
		commStateAtCmd = new CommStateAtCmd(io, CommConfig.respMode);
		sockAtCmd = new SocketAtCmd(io, CommConfig.respMode);
	}

	public DefaultAtCmd getDefaultAtCmd() {
		return defaultAtCmd;
	}

	public ConfigurationAtCmd getConfAtCmd() {
		return confAtCmd;
	}

	public CommStateAtCmd getCommStateAtCmd() {
		return commStateAtCmd;
	}

	public SocketAtCmd getSockAtCmd() {
		return sockAtCmd;
	}
	
	public void closeIO() {
		io.closeIO();
	}

//	@Override
//	public void run() {
//		logger.trace("Start run method");
//
//		while (true) {
//			logger.trace("Start to check communication state");

//			commState = CommState.NEUTRAL;
//			logger.debug("Communication state is \"{}\"", commState.toString());
//
//			if (commStateAtCmd.checkCREG() > 0) {
//				if (commStateAtCmd.checkCSQ() > 0) {
//					commState = CommState.CONNECTED;
//					logger.debug("Communication state is \"{}\"",
//							commState.toString());
//				} else {
//					commState = CommState.NOT_CONNECTED;
//					logger.debug("Communication state is \"{}\"",
//							commState.toString());
//				}
//			} else {
//				commState = CommState.NOT_CONNECTED;
//				logger.debug("Communication state is \"{}\"",
//						commState.toString());
//			}

//			synchronized (this) {
//				if (abort)
//					break;
//			}
//
//			try {
//				Thread.sleep(CommConfig.statusChkPeriod * 1000);
//			} catch (InterruptedException e) {
//				logger.error("Can't wait time");
//				logger.error("Error: {}", e.toString());
//			}
//		}
//
//		logger.trace("End run method");
//	}
//
//	public void abort() {
//		this.abort = true;
//	}



}
