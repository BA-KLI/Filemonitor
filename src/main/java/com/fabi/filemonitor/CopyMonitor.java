/**
 * 
 */
package com.fabi.filemonitor;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * @author Fabian Kliebhan
 *
 */
public class CopyMonitor implements MonitorInterface{
	Logger  logger = Logger.getLogger(CopyMonitor.class);
	Path sourceDir;
	List<Path> targetDirs = new ArrayList<Path>();
	HashMap<String, Boolean> parameters = new HashMap<String, Boolean>();
	
	ArrayList<ArrayList<Path>> filesInTargetsNotInSource = new ArrayList<ArrayList<Path>>();
	ArrayList<ArrayList<Path>> filesInSourceNotInTargets = new ArrayList<ArrayList<Path>>();
	
	
	public CopyMonitor(Path sourceDir, Path targetDir) throws IOException {
		setDirectory(sourceDir);
		targetDirs.add(targetDir);
		init();
	}
	
	/**
	 * @param quellPfad
	 * @param targetPfade
	 * @throws IOException 
	 */
	public CopyMonitor(String quellPfad, List<String> targetPfade) throws IOException {
		this.sourceDir = Paths.get(quellPfad);
		for(String targetPfad : targetPfade){
			targetDirs.add(Paths.get(targetPfad));
		}
		init();
	}

	/**
	 * @param quellPfad
	 * @param targetPfade
	 * @param parameters
	 * @throws IOException 
	 */
	public CopyMonitor(String quellPfad, List<String> targetPfade,
			HashMap<String, Boolean> parameters) throws IOException {
		this.sourceDir = Paths.get(quellPfad);
		for(String targetPfad : targetPfade){
			targetDirs.add(Paths.get(targetPfad));
		}
		this.parameters.putAll(parameters);
		init();
	}
	
	
	public void init() throws IOException{
			if(targetDirs.size() == 0){
				logger.error("No target is set");
				return;
			}
			
            this.filesInTargetsNotInSource = new ArrayList<ArrayList<Path>>();
            this.filesInSourceNotInTargets = new ArrayList<ArrayList<Path>>();
            
            for(Path targetDir : targetDirs){
            	this.filesInTargetsNotInSource.add(collectRelativeFilesInSrc1NotPresentInSrc2(targetDir,sourceDir));
            	this.filesInSourceNotInTargets.add(collectRelativeFilesInSrc1NotPresentInSrc2(sourceDir,targetDir));
            }
	}

	/**
	 * @param files
	 * @return
	 */
	private String arrayToString(ArrayList<Path> array) {
		String s = "";
		
		for(Path path : array)
			s += path.toString()+"\n";
		return s;
	}


	public String listFilesFromTargetNotInSource(int targetno) throws IOException{
		return arrayToString(filesInTargetsNotInSource.get(targetno));
	}
	

	public ArrayList<String> listFilesFromTargetsNotInSource() throws IOException{
		ArrayList<String> strings = new ArrayList<String>();
		for(int i=0;i<targetDirs.size();i++){
			strings.add(listFilesFromTargetNotInSource(i));
		}
		return strings;
	}
	
	
	public String listFilesFromSourceNotInTarget(int targetno) throws IOException{
		return arrayToString(filesInSourceNotInTargets.get(targetno));
	}
	

	public ArrayList<String> listFilesFromSourceNotInTargets() throws IOException{
		ArrayList<String> strings = new ArrayList<String>();
		for(int i=0;i<targetDirs.size();i++){
			strings.add(listFilesFromSourceNotInTarget(i));
		}
		return strings;
	}
	
	
	

	public ArrayList<LinkedHashMap<Path, Patch>> listFilesDiffsFromSourcewToTarget() throws IOException{
		ArrayList<LinkedHashMap<Path, Patch>> patches = new ArrayList<LinkedHashMap<Path,Patch>>();
		
		for(int i=0;i<targetDirs.size();i++)
			patches.add(getFileDiffsOfDirectories(sourceDir,targetDirs.get(i)));
		
		return patches;
	}
	
	public String diffsToString(LinkedHashMap<Path, Patch> diffHashMap){
		String s = "";
		
		for(Path p : diffHashMap.keySet()){
			s += patchToString(p, diffHashMap.get(p));
		}
		return s;
	}

	public String patchToString(Path path, Patch patch){
		String s = path.toString()+" differs:\n";
		for(Delta d : patch.getDeltas())
			s+= d.toString()+"\n";
		return s;
	}
	
	
	
	public void copyFilesFromTargetNotInSource(int targetid) throws IOException{
		Path targetDir = targetDirs.get(targetid);
		copyFiles(targetDir,sourceDir,filesInTargetsNotInSource.get(targetid));
	}
	
	public void copyFilesFromSourceNotInTarget(int targetid) throws IOException{
		Path targetDir = targetDirs.get(targetid);
		copyFiles(sourceDir,targetDir,filesInSourceNotInTargets.get(targetid));
	}
	
	
	
	public void copyFiles(Path from, Path to, ArrayList<Path> files) throws IOException{
		for(Path path : files){
			Path sourcePath = from.resolve(path);
			Path targetPath = to.resolve(path);
			Files.createDirectories(targetPath.getParent());
			Files.copy(sourcePath, targetPath, COPY_ATTRIBUTES);
			logger.debug("cp "+sourcePath+" "+targetPath);
		}
		
	}
	
	/**
	 * @param src1
	 * @param src2
	 * @throws IOException 
	 */
	protected ArrayList<Path> collectRelativeFilesInSrc1NotPresentInSrc2(final Path src1, final Path src2) throws IOException {
		
	final ArrayList<Path> src1ToSrc2Files = new ArrayList<Path>();
		
        Files.walkFileTree(src1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
            	if(dir.getFileName().toString().startsWith("."))
            		return FileVisitResult.SKIP_SUBTREE;
            	
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs)
                      throws IOException{

            	if(file.getFileName().toString().startsWith("."))
                    return FileVisitResult.CONTINUE;

				Path subPath = src1.relativize(file);
				Path newPath = src2.resolve(subPath);
            	
				if(Files.notExists(newPath)){
					src1ToSrc2Files.add(subPath);
				}
            	
                return FileVisitResult.CONTINUE;
            	
            }
        });
        return src1ToSrc2Files;
	}

	
	
	
	protected LinkedHashMap<Path, Patch> getFileDiffsOfDirectories(final Path src1, final Path src2) throws IOException {
		
		final LinkedHashMap<Path, Patch> src1ToSrc2Files = new LinkedHashMap<Path,Patch>();
		
        Files.walkFileTree(src1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
            	if(dir.getFileName().toString().startsWith("."))
            		return FileVisitResult.SKIP_SUBTREE;
            	
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs)
                      throws IOException{

            	if(file.getFileName().toString().startsWith("."))
                    return FileVisitResult.CONTINUE;

				Path subPath = src1.relativize(file);
				Path newPath = src2.resolve(subPath);
            	
				if(Files.exists(newPath)){

					boolean compare = true;
					
					for(Path f : Arrays.asList(file,newPath)){
						long kbFile = Files.size(f)/1024;
						if(kbFile > 200){
							logger.warn("File "+f +" is "+kbFile+"kB big and will not be compared");
							compare = false;
						}
					}
					
					if(compare){
						Patch patch = DiffUtils.diff(fileToLines(file.toString()), fileToLines(newPath.toString()));
						
	
	
					    if(patch.getDeltas().size() != 0)
					    	src1ToSrc2Files.put(subPath, patch);
					}
				}
            	
                return FileVisitResult.CONTINUE;
            	
            }
        });
        return src1ToSrc2Files;
	}

	

    // Helper method for get the file content
    private List<String> fileToLines(String filename) {
            List<String> lines = new LinkedList<String>();
            String line = "";
            try {
                    BufferedReader in = new BufferedReader(new FileReader(filename));
                    while ((line = in.readLine()) != null) {
                            lines.add(line);
                    }
                    in.close();
            } catch (IOException e) {
                    e.printStackTrace();
        			logger.error(e.getMessage());
            }
            return lines;
    }

	
	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#monitorFileCreate(java.nio.file.Path)
	 */
	public void monitorFileCreate(Path file) {
		try {
			
			if(file.startsWith(sourceDir)){
				Path subPath = sourceDir.relativize(file);
				for(Path targetDir : targetDirs){
					Path newPath = targetDir.resolve(subPath);
					Files.createDirectories(newPath.getParent());
					logger.info("cp "+file.toString()+" "+newPath.toString());
					
					Files.copy(file, newPath, COPY_ATTRIBUTES);
				}
			}
			
		} catch (IOException e) {
            e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#monitorFileModify(java.nio.file.Path)
	 */
	public void monitorFileModify(Path file) {
		try {
			
			if(file.startsWith(sourceDir) && !Files.isDirectory(file)){
				Path subPath = sourceDir.relativize(file);
				for(Path targetDir : targetDirs){
					Path newPath = targetDir.resolve(subPath);
					Files.createDirectories(newPath.getParent());
					logger.info("cp "+file.toString()+" "+newPath.toString());
					
					Files.copy(file, newPath, REPLACE_EXISTING, COPY_ATTRIBUTES);
				}
			}
			
		} catch (IOException e) {
            e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#monitorFileDelete(java.nio.file.Path)
	 */
	public void monitorFileDelete(Path file) {
try {
			
			if(file.startsWith(sourceDir)){
				Path subPath = sourceDir.relativize(file);
				for(Path targetDir : targetDirs){
					Path newPath = targetDir.resolve(subPath);
					logger.info("rm "+newPath.toString());
					Files.delete(newPath);
				}
			}
			
		} catch (IOException e) {
            e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#setDirectory(java.nio.file.Path)
	 */
	public void setDirectory(Path path) {
		this.sourceDir = path;
	}

	/* (non-Javadoc)
	 * @see com.fabi.filemonitor.MonitorInterface#getDirectory()
	 */
	public Path getDirectory() {
		return sourceDir;
	}

}
