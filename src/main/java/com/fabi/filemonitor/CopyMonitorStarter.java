/**
 * 
 */
package com.fabi.filemonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Fabian Kliebhan
 *
 */
public class CopyMonitorStarter implements Runnable {
	Logger  logger = Logger.getLogger(CopyMonitorStarter.class);

	private String quellPfad;
	private List<String> targetPfade;
	private HashMap<String, Boolean> parameters = new HashMap<String, Boolean>();
	
	private CopyMonitor copyMonitor;
	

	/**
	 * @param parameters 
	 * 
	 */
	public CopyMonitorStarter(String quellPfad, List<String> targetPfade, HashMap<String, Boolean> parameters) {
		this.quellPfad = quellPfad;
		this.targetPfade = targetPfade;
		this.parameters = parameters;
		try {
			copyMonitor = new CopyMonitor(quellPfad, targetPfade,parameters);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}
	
	public void run() {

		try {
			new DirectoryMonitor(copyMonitor).processEvents();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
	

	public CopyMonitor getCopyMonitor() {
		return copyMonitor;
	}

	public void setCopyMonitor(CopyMonitor copyMonitor) {
		this.copyMonitor = copyMonitor;
	}

}
