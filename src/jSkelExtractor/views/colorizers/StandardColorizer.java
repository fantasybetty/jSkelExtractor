package jSkelExtractor.views.colorizers;

import java.awt.Color;

import soam.Vertex;

public class StandardColorizer implements IColorizer {

	@Override
	public Color getColorFor(Vertex vertex) {
		return Color.RED;
	}

}
