package org.csnpod.comm.socket;

public interface SocketConnector {
	
	public int connect(String addr, int port);
	
	public int close();
	
	public String read(int byteNum);
	
	public int write(String data);
	
	public String getLocalAddr();
	
	public String getRemoteAddr();
	
	public int getLocalPort();
	
	public int getRemotePort();

}
