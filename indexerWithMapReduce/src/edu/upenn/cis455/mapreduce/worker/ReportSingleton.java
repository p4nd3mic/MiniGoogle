package edu.upenn.cis455.mapreduce.worker;


public class ReportSingleton {
	private static final ReportSingleton info = new ReportSingleton();
	private String remoteAddr = null;
	private String portNum =  null;
	private ReportMessage reportMessage;
	
	private ReportSingleton() {

	}

	public static ReportSingleton getInstance() {
		return info;
	}
	
	public void startReportMessage(){
		if(remoteAddr != null && portNum != null){
			reportMessage = new ReportMessage(remoteAddr, portNum);
			reportMessage.start();
		}
	}
	
	public ReportMessage getReportMessage(){
		return reportMessage;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public void setPortNum(String portNum) {
		this.portNum = portNum;
	}
}
