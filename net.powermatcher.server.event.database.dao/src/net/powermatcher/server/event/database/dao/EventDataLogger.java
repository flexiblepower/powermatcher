package net.powermatcher.server.event.database.dao;


import java.util.logging.Logger;

/**
 * Event Monitor logger.
 */
public class EventDataLogger {

	private static EventDataLogger myLogger;

	private Logger myJavaUtilLogger;
	
	private EventDataLogger() {
		myJavaUtilLogger = Logger.getLogger(EventDataLogger.class.getName());
	}

	/* (non-Javadoc)
	 * @see com.ibm.rfid.premises.logger.BaseLogger#_init()
	 */
	protected void _init() {
		
	}

	/**
	 * Returns the singleton EMLogger instance
	 * @return singleton EMLogger instance
	 */
	public static EventDataLogger singleton() {
		if (myLogger == null) {
			myLogger = new EventDataLogger();
		}
		
		return myLogger;
	}

	public void textMessage(long arg0, Object arg1, String arg2, String arg3) {
		myJavaUtilLogger.info(arg0 + " " + arg1 + " " + arg2 + " " + arg3);
		
	}

	public void trace(long arg0, Object arg1, String arg2, String arg3) {
		myJavaUtilLogger.fine(arg0 + " " + arg1 + " " + arg2 + " " + arg3);
		
	}

	public void traceEntry(Object arg0, String arg1) {
		myJavaUtilLogger.entering(arg0.toString(), arg1);
	}

	public void traceExit(Object arg0, String arg1) {
		myJavaUtilLogger.exiting(arg0.toString(), arg1);
	}

	
}
