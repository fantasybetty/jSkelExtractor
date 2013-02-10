
/**
 *  Copyright 2008 Universita' degli Studi di Pavia
 *  Laboratorio di Visione Artificiale
 *  http://vision.unipv.it
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package jSkelExtractor.algorithms;

import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.views.colorizers.IColorizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import soam.Mesh;


public abstract class Skeletrizer extends Observable implements IComposedAlgorithm, Observer{


	protected Mesh mesh;
	protected ArrayList<AbstractAlgorithm> algorithms;
	protected int currentStep;
	protected IColorizer colorizer;
	
	protected boolean loop;
	protected boolean isFinished;
	protected ConfigObj configObj;
	private long startTime;
	private PrintStream console;

	
	public Skeletrizer(Mesh mesh){
		configObj = ConfigObj.getInstance();
		this.mesh = mesh;
		this.algorithms = new ArrayList<AbstractAlgorithm>();
		this.currentStep = -1;
		this.loop = configObj.isLooping();
		this.isFinished = false;
		console = System.out;
	}

	
	public void setAlgorithm(AbstractAlgorithm algorithm){
		algorithm.addObserver(this);
		this.algorithms.add(algorithm);
	}
	
	public ArrayList<AbstractAlgorithm> getAlgorithmList(){
		return this.algorithms;
	}

	@Override
	public AbstractAlgorithm getExecutingAlgorithm() {
		return this.algorithms.get(currentStep);
	}

	@Override
	public void next() {
		this.currentStep++;
		if(this.currentStep < algorithms.size()){
			if(this.getExecutingAlgorithm().getEnableStatus())
				this.getExecutingAlgorithm().execute();
			else this.next();
		}else{
			this.isFinished = true;
			console.println("Total Execution Time(s): "+(Calendar.getInstance().getTimeInMillis()-startTime)/1000.0);
			if(configObj.isLooping()) {
				this.reset();
				this.start();
			}
		}

	}

	@Override
	public Mesh mesh() {
		return this.mesh;
	}


	@Override
	public void update(Observable arg0, Object arg1) {
		this.previousAlgorithmCompleted();
		if(!configObj.isInteractiveExecution()) {
			this.next();

		}else{

		}
		setChanged();
		notifyObservers();

	}
	
	public void setColorizer(IColorizer colorizer){
		this.colorizer = colorizer;
	}
	
	@Override
	public IColorizer colorizer(){
		return this.colorizer;
	}

	@Override
	public int  getAlgorithmsNumber(){
		return this.algorithms.size();
	}

	@Override
	public void start() {
		this.startTime = Calendar.getInstance().getTimeInMillis();
		this.next();
	}
	
	@Override
	public void reset(){
		configObj.getResContainer().reset();
		for(AbstractAlgorithm aa:algorithms){
			aa.reset();
		}
		this.isFinished = false;
		this.currentStep = -1;
		//this.start();
	}

	public boolean isLoop() {
		return loop;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	
	@Override
	public boolean isFinished() {
		return this.isFinished;
	}

}
