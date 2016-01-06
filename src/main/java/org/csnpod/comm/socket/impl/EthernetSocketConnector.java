package org.csnpod.comm.socket.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;

import org.csnpod.comm.atcmd.CommStateAtCmd;
import org.csnpod.comm.socket.SocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthernetSocketConnector implements SocketConnector {
	private Logger logger = LoggerFactory.getLogger(EthernetSocketConnector.class);
	private Socket sock;
	private String remoteAddr;
	private int remotePort;
	private BufferedReader br;
	private PrintWriter pw;

	@Override
	public int connect(String addr, int port) {
		logger.trace("Start connect method");
		
		this.remoteAddr = addr;
		this.remotePort = port;
		try {
			sock = new Socket(addr, port);
			InputStreamReader isr = new InputStreamReader(sock.getInputStream());
			br = new BufferedReader(isr);

			OutputStreamWriter osw = new OutputStreamWriter(
					sock.getOutputStream());
			pw = new PrintWriter(osw);

		} catch (UnknownHostException e) {
			logger.error("Can't connect to \"{}\" because of Unknown Host Exception", addr);
			logger.error("Error: {}", e.toString());
			return -1;
		} catch (IOException e) {
			logger.error("Can't connect to \"{}\" because of IO Expcetion", addr);
			logger.error("Error: {}", e.toString());
			return -1;
		}
		
		
		logger.trace("End connect method");
		return 1;
	}

	@Override
	public int close() {
		logger.trace("Start close method");
		
		try {
			br.close();
			pw.close();
			sock.close();
		} catch (IOException e) {
			logger.error("Can't close stream because of IO Expcetion");
			logger.error("Error: {}", e.toString());
			return -1;
		}
		
		logger.trace("End close method");
		return 0;
	}

	@Override
	public String read(int byteNum) {
		logger.trace("Start read method");
		
		String str = "";
		try {
			str = br.readLine();
			logger.debug("Read data: \"{}\"", str);
		} catch (IOException e) {
			logger.error("Can't Read stream because of IO Expcetion");
			logger.error("Error: {}", e.toString());
			return null;
		}

		logger.trace("End read method");
		return str;
	}

	@Override
	public int write(String data) {
		logger.trace("Start write method");
		
		logger.debug("\"{}\" will be sended", data);
		pw.println(data);
		pw.flush();
		logger.trace("End write method");
		return 0;
	}

	@Override
	public String getLocalAddr() {
		logger.trace("Start getLocalAddr method");
		String localAddr = null;
		
		try {
			localAddr = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			logger.error("Can't get local address because of Unknown HostExpcetion");
			logger.error("Error: {}", e.toString());
			return localAddr;
		}
		
		logger.trace("En getLocalAddr method");
		return localAddr;
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddr;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}

}
