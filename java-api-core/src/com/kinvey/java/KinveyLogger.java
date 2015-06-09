package com.kinvey.java;

public abstract class KinveyLogger {

	public void info(String message){}
	
	public void debug(String message){}
	
	public void trace(String message){}
	
	public void warning(String message){}
	
	public void error(String message){}
	
	
	public enum CONTEXT{
		NETWORK, //this will effect all modules
		USER,
		OFFLINE, 	
		FILE,
		APPDATA,
		PUSH,
		MIC,
		LINKEDDATA,
		KINVEYREFERENCE,
		CLIENT
	}
	
	

	
	public static class Logger{
		
		private static KinveyLogger log;
		private static boolean isInitialized = false;
		
		public static void init (KinveyLogger log){
			if (log == null){
				throw new KinveyException("Logger can't be null!");
			}
			Logger.log = log;
			isInitialized = true;
		}
		
		public static void INFO(String message){
			if (!isInitialized){
				return;
			}
			log.info(message);
		}
		
		public static void DEBUG(String message){
			if (!isInitialized){
				return;
			}
			log.debug(message);	
		}
		
		public static void TRACE(String message){
			if (!isInitialized){
				return;
			}
			log.trace(message);
		}
		
		public static void WARNING(String message){
			if (!isInitialized){
				return;
			}
			log.warning(message);
		}
		
		public static void ERROR(String message){
			if (!isInitialized){
				return;
			}
			log.error(message);
		}	
	}	
}