package jSkelExtractor.algorithms;

import jSkelExtractor.configs.ConfigObj;

import java.util.Observable;

import soam.Mesh;

public abstract class AbstractAlgorithm extends Observable implements IAlgorithm{
	
	protected Mesh mesh;
	protected boolean isFinished;
	protected boolean isEnabled;
	protected ConfigObj configObj;
	
	public AbstractAlgorithm(Mesh mesh, ConfigObj configObj){
		this.mesh = mesh;
		this.isFinished = false;
		this.configObj = configObj;
		this.isEnabled = true;
	}


	@Override
	public boolean isFinished() {
		return this.isFinished;
	}

	@Override
	public Mesh mesh() {
		return this.mesh;
	}

	@Override
	public void reset(Mesh mesh) {
		this.mesh = mesh;
		this.reset();
	}
	
	@Override
	public String getAlgorithmName(){
		String name = this.getClass().getName();
		return name.substring(name.lastIndexOf(".")+1, name.length());
	}
	
	@Override
	public void setEnableStatus(boolean enablestatus){
		this.isEnabled = enablestatus;
	}
	
	@Override
	public boolean getEnableStatus(){
		return this.isEnabled;
	}

	
	
	
}
