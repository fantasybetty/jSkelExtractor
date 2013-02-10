package jSkelExtractor.controllers;

import jSkelExtractor.algorithms.AbstractAlgorithm;
import jSkelExtractor.algorithms.Skeletrizer;

import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JFrame;

public class ExecutionEditor extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Skeletrizer algoContainer;

	public ExecutionEditor(Skeletrizer algo) {
		this.algoContainer = algo;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("Algorithm Execution Manager");
		
		Box box = Box.createHorizontalBox();
		ArrayList<AbstractAlgorithm> algorithms = this.algoContainer.getAlgorithmList();
		
		for(AbstractAlgorithm aa:algorithms){
			box.add(new AlgoController(aa));
		}
		
		this.getContentPane().add(box);
		
		this.pack();
	}

}
