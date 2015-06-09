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
		
		private static KinveyLogger platformLogger;
		private static boolean isInitialized = false;
		
		public static void init (KinveyLogger log){
			if (log == null){
				throw new KinveyException("Logger can't be null!");
			}
			Logger.platformLogger = log;
			isInitialized = true;
		}
		
		public static void INFO(String message){
			if (!isInitialized){
				return;
			}
			platformLogger.info(message);
		}
		
		public static void DEBUG(String message){
			if (!isInitialized){
				return;
			}
			platformLogger.debug(message);	
		}
		
		public static void TRACE(String message){
			if (!isInitialized){
				return;
			}
			platformLogger.trace(message);
		}
		
		public static void WARNING(String message){
			if (!isInitialized){
				return;
			}
			platformLogger.warning(message);
		}
		
		public static void ERROR(String message){
			if (!isInitialized){
				return;
			}
			platformLogger.error(message);
		}	
	}	
}