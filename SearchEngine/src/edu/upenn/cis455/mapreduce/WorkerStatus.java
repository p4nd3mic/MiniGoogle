package edu.upenn.cis455.mapreduce;

public enum WorkerStatus {

	MAPPING("mapping"),
	WAITING("waiting"),
	REDUCING("reducing"),
	IDLE("idle");
	
	private String name;

	private WorkerStatus(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static WorkerStatus getStatus(String name) {
		WorkerStatus status = null;
		if("mapping".equals(name)) {
			status = WorkerStatus.MAPPING;
		} else if("waiting".equals(name)) {
			status = WorkerStatus.WAITING;
		} else if("reducing".equals(name)) {
			status = WorkerStatus.REDUCING;
		} else if("idle".equals(name)) {
			status = WorkerStatus.IDLE;
		}
		return status;
	}
}
