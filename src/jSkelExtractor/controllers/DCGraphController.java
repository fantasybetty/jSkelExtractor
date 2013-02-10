package jSkelExtractor.controllers;

import jSkelExtractor.algorithms.Dijkstra;
import jSkelExtractor.algorithms.sdskeletrizer.DiscreteContoursGraph;
import jSkelExtractor.configs.ConfigObj;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class DCGraphController extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DiscreteContoursGraph dcAlgo;

	public DCGraphController(DiscreteContoursGraph dcAlgo) {
		super("Discrete Contours Grapher Controller");
		this.dcAlgo = dcAlgo;
		
		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!isFinished()) next();
				else setEnabled(false);
				
			}
			
		});
		
		
		this.getContentPane().add(nextButton);
		this.pack();
		this.setLocation(1170,200);
		this.setVisible(true);
	}
	
	protected void next(){
		dcAlgo.next();
	}
	
	protected boolean isFinished(){
		return dcAlgo.isFinished();
	}

}
