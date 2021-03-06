package com.fabi.filemonitor;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import difflib.Patch;



public class AppWindow extends JFrame {
	Logger  logger = Logger.getLogger(AppWindow.class);

	/**
	 * Default Version Id
	 */
	private static final long serialVersionUID = 1L;
	JTextField 	quellPfad;
	JLabel 	quellPfadLabel;
	

	JTextField 	targetPfad;
	JLabel 	targetPfadLabel;
	
	public final AppWindow self = this;
	
	JButton		button_monitorStarten;

	

	

	List<BufferedImage> imgs = null;

	private JButton quellPfadChooserButton;

	private JButton targetPfadChooserButton;


	private JFileChooser directoryChooser;

	private JCheckBox checkbox_copyTargetToSource;

	private JCheckBox checkbox_copySourceToTarget;

	public AppWindow() {
		
		this.getContentPane().setLayout(null);

		this.initWindow();

		this.addWindowListener(new WindowListener() {

			public void windowClosed(WindowEvent arg0) {


			}

			public void windowActivated(WindowEvent e) {


			}

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowDeactivated(WindowEvent e) {


			}

			public void windowDeiconified(WindowEvent e) {


			}

			public void windowIconified(WindowEvent e) {


			}

			public void windowOpened(WindowEvent e) {


			}



		});

	}

	protected void initWindow() 
	{
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new FlowLayout(1,20,20));

		

		JPanel textPanel = new JPanel();
		JPanel optionPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		textPanel.setLayout(new GridLayout(0, 3));
		optionPanel.setLayout(new GridLayout(0, 1));
		buttonPanel.setLayout(new GridLayout(0, 1));
		
		directoryChooser = new JFileChooser();
		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		

		quellPfadLabel = new JLabel("Quellpfad");
		quellPfad = new JTextField("",20);
		quellPfadChooserButton = new JButton("Choose source directory");
		
		
		targetPfadLabel = new JLabel("Targetpfad");
		targetPfad = new JTextField("",20);
		targetPfadChooserButton = new JButton("Choose target directory");
		
		checkbox_copyTargetToSource = new JCheckBox("Copy files from target that don't exist in source");
		checkbox_copySourceToTarget = new JCheckBox("Copy files from source that don't exist in target");
		
		
		button_monitorStarten = new JButton("Copy-Monitoring starten");

		

		quellPfadChooserButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				directoryChooser.setDialogTitle("Select source directory");
				int returnVal = directoryChooser.showOpenDialog(self);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					quellPfad.setText(directoryChooser.getSelectedFile().getAbsolutePath()); 
				}
			}
				
			

		});

		targetPfadChooserButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				directoryChooser.setDialogTitle("Select target directory");
				int returnVal = directoryChooser.showOpenDialog(self);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					targetPfad.setText(directoryChooser.getSelectedFile().getAbsolutePath());
				}
			}
				
			

		});
		
		button_monitorStarten.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try{
				

				logger.info("Adding Copy Monitor");
				logger.info("Source Path: "+quellPfad.getText());
				logger.info("Copy Path: "+targetPfad.getText());
					
				HashMap<String, Boolean> parameters = new HashMap<String, Boolean>();
				parameters.put("copyTargetToSource", checkbox_copyTargetToSource.isSelected());
				
				List<String> targets = Arrays.asList(targetPfad.getText().split(","));
				CopyMonitorStarter copystarter = new CopyMonitorStarter(quellPfad.getText(),targets,parameters);
				CopyMonitor copymonitor = copystarter.getCopyMonitor();
				
				if(checkbox_copyTargetToSource.isSelected()){
					ArrayList<String> filesInTargetsNotInSource = copymonitor.listFilesFromTargetsNotInSource();
					
					for(int i = 0;i<filesInTargetsNotInSource.size();i++){
						String files = filesInTargetsNotInSource.get(i);
						if(askToCopyFiles(targets.get(i),quellPfad.getText(),files))
							copymonitor.copyFilesFromTargetNotInSource(i);
					}
					
				}
				
				
				if(checkbox_copySourceToTarget.isSelected()){
					ArrayList<String> filesInSourcesNotInTarget = copymonitor.listFilesFromSourceNotInTargets();
					

					for(int i = 0;i<filesInSourcesNotInTarget.size();i++){
						String files = filesInSourcesNotInTarget.get(i);
						if(askToCopyFiles(quellPfad.getText(),targets.get(i),files))
							copymonitor.copyFilesFromSourceNotInTarget(i);
					}
					
				}
				
				ArrayList<LinkedHashMap<Path, Patch>> diffMap = copymonitor.listFilesDiffsFromSourcewToTarget();
				
				int i=0;
				for(LinkedHashMap<Path, Patch> map : diffMap){
					i++; 
					for(Path p : map.keySet()){
						

						String text = "Difference betweeen "+quellPfad.getText()+" und "+targets.get(i-1);
						String diffs = copymonitor.patchToString(p, map.get(p));

						logger.debug("\n\n\n");
						
						text += "\nFile "+p;
						
						JOptionPane.showMessageDialog(null, text+"\n"+(diffs.length() < 1000 ? diffs : diffs.substring(0, 1000)+"..."));

						logger.debug(text);
						logger.debug("\n");
						

						
						logger.debug("------------------------------------------------------------");
						logger.debug(diffs);
						logger.debug("------------------------------------------------------------");
					} 
				}
				
				new Thread(copystarter).start();
				logger.info("Copy Monitor started...");

				}
				catch(IOException err){
					err.printStackTrace();
					logger.error(err.getMessage());
				}
			}

			private boolean askToCopyFiles(String source, String target, String filesStr) {

				String question = "Copy these files from "+source+" to "+target;
				
				//default icon, custom title
				int n = JOptionPane.showConfirmDialog(
				    self,
				    question+"\n\n"+
				    filesStr,
				    "Copy Files?",
				    JOptionPane.YES_NO_OPTION);
				
				if(n == JOptionPane.YES_OPTION){
					logger.debug("Copying unpresent files from "+source+" to "+target);
					return true;
				}
				else if (n == JOptionPane.NO_OPTION){
					return false;
				}
				return false;
			}
				
			

		});

		textPanel.add(quellPfadLabel);
		textPanel.add(quellPfad);
		textPanel.add(quellPfadChooserButton);
		textPanel.add(targetPfadLabel);
		textPanel.add(targetPfad);
		textPanel.add(targetPfadChooserButton);
		optionPanel.add(checkbox_copyTargetToSource);
		optionPanel.add(checkbox_copySourceToTarget);
		buttonPanel.add(button_monitorStarten);
		final JPanel mainPanel = new JPanel();
		mainPanel.setMaximumSize(new Dimension(400,400));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10,0,0,0); 
		mainPanel.add(textPanel,c);
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(10,0,0,0); 
		mainPanel.add(optionPanel,c);
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(10,0,0,0); 
		mainPanel.add(buttonPanel,c);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.NORTH);
		this.getContentPane().add(SwingAppenderUI.getInstance(), BorderLayout.CENTER);
		
		
	}

	public void showMessage(String message){
		JOptionPane.showMessageDialog(null, "Test");
	}
}