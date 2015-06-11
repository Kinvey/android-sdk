package com.kinvey.java;

import java.util.HashMap;
import java.util.logging.Level;

import com.google.api.client.http.HttpTransport;

/**
 * This class is used statically throughout the library.  
 * It will delegate to an instance of a `KinveyLogger`, which does the actual log writing.
 * 
 * @author edward
 *
 */
public class Logger {
	
	/**
	 *  log levels
	 */
	private static final String INFO = "INFO";
	private static final String DEBUG = "DEBUG";
	private static final String TRACE = "TRACE";
	private static final String NETWORK = "NETWORK";
	private static final String WARNING = "WARNING";
	private static final String ERROR = "ERROR";
	
	/***
	 * Singleton pattern
	 * @return
	 */
	private static Logger getInstance(){
		if (_instance == null){
			_instance = new Logger();
		}
		return _instance;
	}
	private static Logger _instance;
	
	/**
	 * The KinveyLogger which does the actual writing
	 */
	private KinveyLogger platformLogger;
	
	/**
	 * Has the Logger been initialized?
	 */
	private boolean isInitialized = false;
	
	/**
	 * This map determines if a log level is on or off.
	 */
	private HashMap<String, Boolean> activeMap;
	
	/**
	 * Initialize this Logger with an instance of a KinveyLogger, defaults to no logging at all.
	 * @param log
	 */
	public static void init (KinveyLogger log){
		if (log == null){
			throw new KinveyException("Logger can't be null!");
		}
		if (getInstance().platformLogger != null){
			return;
		}
		
		getInstance().platformLogger = log;
		
		getInstance().activeMap = new HashMap<String, Boolean>();
		getInstance().activeMap.put(NETWORK, false);
		java.util.logging.Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.OFF);
		getInstance().activeMap.put(INFO, false);
		getInstance().activeMap.put(DEBUG, false);
		getInstance().activeMap.put(TRACE, false);
		getInstance().activeMap.put(WARNING, false);
		getInstance().activeMap.put(ERROR, false);
		
		getInstance().isInitialized = true;
	}
	
	public static Logger configBuilder(){
		return getInstance();
	}
	
	public Logger network(){
		getInstance().activeMap.put(NETWORK, true);
		java.util.logging.Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.FINEST);
		return getInstance();
	}
	
	/**
	 * Enable info level logging
	 * @return the Logger
	 */
	public Logger info(){
		getInstance().activeMap.put(INFO, true);
		return getInstance();
	}
	
	/**
	 * Enable debug level logging
	 * @return the Logger
	 */
	public Logger debug(){
		getInstance().activeMap.put(DEBUG, true);
		return getInstance();
	}
	
	/**
	 * Enable trace level logging
	 * @return the Logger
	 */
	public Logger trace(){
		getInstance().activeMap.put(TRACE, true);
		return getInstance();
	}
	
	/**
	 * Enable warning level logging
	 * @return the Logger
	 */
	public Logger warning(){
		getInstance().activeMap.put(WARNING, true);
		return getInstance();
	}
	
	/**
	 * Enable error level logging
	 * @return the Logger
	 */
	public Logger error(){
		getInstance().activeMap.put(ERROR, true);
		return getInstance();
	}
	
	public Logger all(){
		getInstance().activeMap.put(INFO, true);
		getInstance().activeMap.put(DEBUG, true);
		getInstance().activeMap.put(TRACE, true);
		getInstance().activeMap.put(NETWORK, true);
		java.util.logging.Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.FINEST);
		getInstance().activeMap.put(WARNING, true);
		getInstance().activeMap.put(ERROR, true);
		return getInstance();
	}
	
	/**
	 * Log an info message
	 * @param message
	 */
	public static void INFO(String message){
		if (!getInstance().isInitialized){
			return;
		}
		if (getInstance().activeMap.get(INFO)){
			getInstance().platformLogger.info(message);
		}
	}
	
	/***
	 * Log a debug message
	 * @param message
	 */
	public static void DEBUG(String message){
		if (!getInstance().isInitialized){
			return;
		}
		if (getInstance().activeMap.get(DEBUG)){
			getInstance().platformLogger.debug(message);	
		}
	}
	
	/**
	 * Log a trace message
	 * @param message
	 */
	public static void TRACE(String message){
		if (!getInstance().isInitialized){
			return;
		}
		if (getInstance().activeMap.get(TRACE)){
			getInstance().platformLogger.trace(message);
		}
	}
	
	/**
	 * log a warning message
	 * @param message
	 */
	public static void WARNING(String message){
		if (!getInstance().isInitialized){
			return;
		}
		if (getInstance().activeMap.get(WARNING)){
			getInstance().platformLogger.warning(message);
		}
	}
	
	/**
	 * Log an error message
	 * @param message
	 */
	public static void ERROR(String message){
		if (!getInstance().isInitialized){
			return;
		}
		if (getInstance().activeMap.get(ERROR)){
			getInstance().platformLogger.error(message);
		}
	}	


	/***
	 * This interface defines the behaivor of a platform specific logger
	 * @author edward
	 */
	public static interface KinveyLogger{
		
		/**
		 * Time to write an info message to the output
		 * @param message
		 */
		public void info(String message);
		
		/**
		 * Time to write a debug message to the output
		 * @param message
		 */
		public void debug(String message);
		
		/**
		 * Time to write a trace message to the output
		 * @param message
		 */
		public void trace(String message);
		
		/**
		 * Time to write a warning message to the output
		 * @param message
		 */
		public void warning(String message);
		
		/**
		 * Time to write an error message to the output
		 * @param message
		 */
		public void error(String message);

	}	
}