package jSkelExtractor.controllers;

import jSkelExtractor.algorithms.Skeletrizer;
import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.configs.ViewConfigObj;
import jSkelExtractor.views.BeanEditor;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class SkeletrizeController extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Skeletrizer algo;
	private ConfigObj configObj;

	private JButton startButton;
	private JCheckBox isinteractive;
	private JButton nextButton;
	private JButton optionsButton;

	protected BeanEditor optionsEditor;
	protected ExecutionEditor executionEditor;

	private JButton executionButton;

	public SkeletrizeController(Skeletrizer algorithm) {
		this.algo = algorithm;
		this.configObj = ConfigObj.getInstance();
		this.optionsEditor = null;

		this.algo.addObserver(this);

		startButton = new JButton("Start Execution");
		isinteractive = new JCheckBox("Interactive Execution?", configObj
				.isInteractiveExecution());
		nextButton = new JButton("Next");
		optionsButton = new JButton("Configuration Options");
		executionButton = new JButton("Execution Options");

		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				algo.reset();
				algo.start();
				// startButton.setEnabled(false);
			}

		});

		isinteractive.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configObj.setInteractiveExecution(!configObj
						.isInteractiveExecution());
				nextButton
						.setEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		nextButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				algo.next();

			}

		});
		nextButton.setEnabled(configObj.isInteractiveExecution());

		optionsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (optionsEditor == null)
					optionsEditor = new BeanEditor(configObj);
				optionsEditor.setLocation(1170, 95);
				optionsEditor.setVisible(true);

			}

		});

		executionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (executionEditor == null)
					executionEditor = new ExecutionEditor(algo);
				executionEditor.pack();
				executionEditor.setLocation(0, 700);
				executionEditor.setVisible(true);
			}

		});

		JLabel optionLabel = new JLabel("Configuration:");
		JLabel runLabel = new JLabel("Running Options:");
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(runLabel)
				.addGroup(layout.createSequentialGroup().addComponent(startButton).addComponent(isinteractive).addComponent(nextButton))
				.addComponent(optionLabel)
				.addGroup(layout.createSequentialGroup().addComponent(executionButton).addComponent(optionsButton))
				);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(runLabel)
				.addGroup(layout.createParallelGroup().addComponent(startButton).addComponent(isinteractive).addComponent(nextButton))
				.addGap(20)
				.addComponent(optionLabel)
				.addGroup(layout.createParallelGroup().addComponent(executionButton).addComponent(optionsButton))
				);

	}

	@Override
	public void update(Observable arg0, Object arg1) {

	}

}
