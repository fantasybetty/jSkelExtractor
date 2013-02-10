package jSkelExtractor.controllers;

import jSkelExtractor.algorithms.Dijkstra;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class DijkstraController extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Dijkstra dijkstra;

	public DijkstraController(Dijkstra dijkstra) {
		super("Dijkstra Controller");
		this.dijkstra = dijkstra;
		
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
		this.setVisible(true);
	}
	
	protected void next(){
		dijkstra.execute();
	}
	
	protected boolean isFinished(){
		return dijkstra.isFinished();
	}

}
