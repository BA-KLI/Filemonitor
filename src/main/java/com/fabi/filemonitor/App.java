package com.fabi.filemonitor;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class App 
{
    public static void main(String[] args) throws IOException {

        
        if(args.length == 2){
            // Set up a simple configuration that logs on the console.
            BasicConfigurator.configure();

	        // register directory and process its events
	        new DirectoryMonitor(new CopyMonitor(Paths.get(args[0]), Paths.get(args[1]))).processEvents();;
        }
        else{

    		SwingAppender fa = new SwingAppender();
	    	  fa.setName("SwingLogger");
	    	  fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
	    	  fa.setThreshold(Level.DEBUG);
	    	  fa.activateOptions();
	
	    	//add appender to any Logger (here is root)
	    	Logger.getRootLogger().addAppender(fa);
        	

    		AppWindow theAppWindow = new AppWindow();
    		theAppWindow.setVisible(true);

    		theAppWindow.setSize(800, 600);
        }
    }
}
