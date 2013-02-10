package jSkelExtractor.algorithms;

import jSkelExtractor.views.colorizers.IColorizer;

import java.util.ArrayList;

import soam.Mesh;


public interface IComposedAlgorithm{
	
	public void next();
	
	public AbstractAlgorithm getExecutingAlgorithm();
	
	public void setAlgorithm(AbstractAlgorithm algorithm);
	
	public ArrayList<AbstractAlgorithm> getAlgorithmList();
	
	public int getAlgorithmsNumber();
	
    public Mesh mesh();

	public void start();
	
	public IColorizer colorizer();
	
	public void previousAlgorithmCompleted();

	void reset();

	public boolean isFinished();
	
}
