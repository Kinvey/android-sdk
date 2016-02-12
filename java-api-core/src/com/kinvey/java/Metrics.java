package com.kinvey.java;

import java.util.HashMap;

public class Metrics {
	
	private static HashMap<String, Metrics> metricMap;
	
	public static Metrics getMetrics(String className){
		if (metricMap == null){
			metricMap = new HashMap<String, Metrics>();
		}
		if(!metricMap.containsKey(className)){
			metricMap.put(className, new Metrics());
		}
		
		return metricMap.get(className);
	}
	
	
	
	private HashMap<String, Metric> events;
	
	public void start(String event){
		long start = System.currentTimeMillis();
		events.get(event).setStart(start);
	}
	
	public void end(String event){
		long end = System.currentTimeMillis();
		events.get(event).setEnd(end);
			
	}
	
	
	
	private Metrics(){
		events = new HashMap<String, Metrics.Metric>();
	}

	
	
	private static class Metric{
		
		private long start = -1l;
		private long end = -1l;
		private long delta = -1l;
		private boolean calculated = false;
		
		private Metric(){}

		public long getStart() {
			return start;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public long getEnd() {
			return end;
		}

		public void setEnd(long end) {
			this.end = end;
			if (this.start == -1l){
				return;
			}
			this.calculated = true;
			this.delta = this.end - this.start;	
		}

		public long getDelta() {
			return delta;
		}

		public boolean isCalculated() {
			return calculated;
		}		
	}
	
	
	

}
