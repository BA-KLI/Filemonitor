/**
 * 
 */
package com.fabi.filemonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * @author Fabian Kliebhan
 *
 */
public class TargetFileWatcher  implements MonitorInterface{
	Logger  logger = Logger.getLogger(TargetFileWatcher.class);
	private Path sourcePath;
	private Path targetPath;
	/**
	 * 
	 */
	public TargetFileWatcher(String sourcePath,String targetPath) {
		logger.info("Setting source "+sourcePath);
		this.sourcePath = Paths.get(sourcePath);
		this.targetPath = Paths.get(targetPath);
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#monitorFileCreate(java.nio.file.Path)
	 */
	public void monitorFileCreate(Path file) {
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#monitorFileModify(java.nio.file.Path)
	 */
	public void monitorFileModify(Path file){


		if(file.startsWith(targetPath) && 
		   !Files.isDirectory(file)){
			Path subPath = targetPath.relativize(file);
			Path newPathSource = sourcePath.resolve(subPath);
			Path newPathTarget = targetPath.resolve(subPath);
			
			
			try {

                if(!FileUtils.contentEquals(newPathSource.toFile(),newPathTarget.toFile())){

					logger.debug("Target modified");
						
					logger.debug("Pfad 1 : "+newPathSource.toString());
					logger.debug("Pfad 2 : "+newPathTarget.toString());
					
					logger.debug("Target content:");
					logger.debug(FileUtils.readFileToString(newPathTarget.toFile()));
					logger.debug("End Target content--");
					
					File backup = new File(newPathSource.toFile().getAbsolutePath()+".bak");
					FileUtils.copyFile(newPathSource.toFile(), backup);
                    FileUtils.copyFile(newPathTarget.toFile(), newPathSource.toFile());
						
					JOptionPane.showMessageDialog(App.window, subPath+" got Modified in target "+targetPath+". Copied the target to your source and made a backup of the source to "+backup);
					
			
					java.awt.EventQueue.invokeLater(new Runnable() {
					    public void run() {
					    	App.window.toFront();
					    	App.window.repaint();
					    }
					});
					
					
				}
				
	
			} catch (IOException e) {
				JOptionPane.showMessageDialog(App.window, e.getMessage());
				e.printStackTrace();
			}
			}
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#monitorFileDelete(java.nio.file.Path)
	 */
	public void monitorFileDelete(Path child) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#setDirectory(java.nio.file.Path)
	 */
	public void setDirectory(Path path) {
		this.targetPath = path;
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#getDirectory()
	 */
	public Path getDirectory() {
		return targetPath;
	}

}
