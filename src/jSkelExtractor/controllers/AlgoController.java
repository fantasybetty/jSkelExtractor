package jSkelExtractor.controllers;

import jSkelExtractor.algorithms.AbstractAlgorithm;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class AlgoController extends JPanel {
	
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private Skeletrizer algoContainer;
	private AbstractAlgorithm thisAlgo;
	private JCheckBox statusButton;

	public AlgoController(/*Skeletrizer algoContainer,*/ AbstractAlgorithm thisAlgo){
	//	this.algoContainer = algoContainer;
		this.thisAlgo = thisAlgo;
		statusButton  = new JCheckBox(thisAlgo.getAlgorithmName());
		statusButton.setSelected(thisAlgo.getEnableStatus());
		
		statusButton.addItemListener(
			    new ItemListener() {
			        public void itemStateChanged(ItemEvent e) {
			            boolean value = (e.getStateChange() == ItemEvent.SELECTED);	
			            setAlgoStatus(value);
			        }
			    }
			);

		
		this.add(statusButton);
	}
	
	protected void setAlgoStatus(boolean status){
		this.thisAlgo.setEnableStatus(status);
	}

}
