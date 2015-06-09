package com.kinvey.nativejava;

import com.kinvey.java.Logger.KinveyLogger;


public class JavaLogger implements KinveyLogger{

	@Override
	public void info(String message) {
		System.out.println("Kinvey INFO " + message);
	}

	@Override
	public void debug(String message) {
		System.out.println("Kinvey DEBUG " + message);		
	}

	@Override
	public void trace(String message) {
		System.out.println("Kinvey TRACE " + message);		
	}

	@Override
	public void warning(String message) {
		System.out.println("Kinvey WARNING " + message);		
	}

	@Override
	public void error(String message) {
		System.out.println("Kinvey ERROR " + message);		
	}

}
