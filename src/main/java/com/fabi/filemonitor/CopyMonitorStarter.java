/**
 * 
 */
package com.fabi.filemonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowFieldMatrix;
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
	List<TargetFileWatcher> targetMonitors = new ArrayList<TargetFileWatcher>();

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

			logger.info("creating source");
			targetMonitors = new ArrayList<TargetFileWatcher>();
			for(String targetPfad : targetPfade){
				this.targetMonitors.add(new TargetFileWatcher(quellPfad, targetPfad));
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}
	
	public void run() {


			
			
			
			new Thread(new Runnable() {
				
				public void run() {
					try {
						logger.debug("creating source monitor");
						new DirectoryMonitor(copyMonitor).processEvents();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}).start();
			

			logger.debug("creating target monitor");

			new Thread(new Runnable() {
				
				public void run() {
					try {
						for(TargetFileWatcher targetFileWatcher  : targetMonitors){
							new DirectoryMonitor(targetFileWatcher).processEvents();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}).start();
			
	}
	

	public CopyMonitor getCopyMonitor() {
		return copyMonitor;
	}

	public void setCopyMonitor(CopyMonitor copyMonitor) {
		this.copyMonitor = copyMonitor;
	}

}
