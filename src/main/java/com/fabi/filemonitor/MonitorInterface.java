/**
 * 
 */
package com.fabi.filemonitor;

import java.nio.file.Path;

/**
 * @author Fabian Kliebhan
 *
 */
public interface MonitorInterface {

	public void monitorFileCreate(Path file);

	public void monitorFileModify(Path child);

	public void monitorFileDelete(Path child);

	public void setDirectory(Path path);
	
	public Path getDirectory();
}
