package jSkelExtractor.test;

import jSkelExtractor.algorithms.IComposedAlgorithm;
import jSkelExtractor.algorithms.sdskeletrizer.SDSkeletrizer;
import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.configs.ViewConfigObj;
import jSkelExtractor.controllers.SkeletrizeController;
import jSkelExtractor.utils.GraphicsFileExport;
import jSkelExtractor.utils.MeshIO;
import jSkelExtractor.views.MeshPanel;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import soam.Mesh;


public class StartingPointTester01 extends JFrame{

	private static final long serialVersionUID = 931946924323213823L;
	protected static JFileChooser fileChooser;
	
	protected MeshPanel view;
	protected SkeletrizeController controller;
	
	public StartingPointTester01(File serializedMesh) {
		Mesh mesh = MeshIO.importSerialized(serializedMesh);
		ConfigObj configObj = ConfigObj.getInstance();
		ViewConfigObj vco = ViewConfigObj.getInstance();
		
		double epsilon = 0.04;
		double vertexNeighborhoodDivergence = 0.0;
		vco.put("showDistanceMap", false);
		vco.put("firstMaximumSetDistanceMap", false);
		vco.put("secondMaximumSetDistanceMap", false);
		vco.put("showFeaturePoints", true);
		vco.put("showDiameter", false);
		vco.put("showStartingPoint", true);
		vco.put("showFirstMaxPts", false);
		vco.put("showSecondMaxPts", false);
		
		
		//Experiment setup
		configObj.setInteractiveExecution(false);
		configObj.setLooping(false);

		configObj.setEpsilon(epsilon);
		configObj.setVertexNeighborhoodDivergence(vertexNeighborhoodDivergence);
		
		IComposedAlgorithm skeletrizer = new SDSkeletrizer(mesh);
		view = new MeshPanel(configObj.getResContainer(),skeletrizer,"");
		view.setNODE_RADIUS(0.2);
		view.magnification = view.magnification + 0.5;
		
		fileChooser = new JFileChooser("Select Directory");
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(true);

		
		File dstFile;
		if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
			
			for(int index=0;index <mesh.vertexlist.size();index++){
				configObj.setStartingPoint(index);
				skeletrizer.reset();
				this.getContentPane().add(view.getWindowPanel());
				this.pack();
				
				
				skeletrizer.start();
				while(!skeletrizer.isFinished());
				
				dstFile = new File(fileChooser.getSelectedFile().getPath()+"\\file"+index+".png");
				if (!GraphicsFileExport.exportPNG(view,dstFile, view.getWidth(),view.getHeight())) 
					System.out.println("Could not export to \""+ dstFile.getPath() + "\"");
				else
					System.out.println("Saved "+dstFile.getPath());

			}
		
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		fileChooser = new JFileChooser("Select Ply Mesh");
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Serialized Mesh","serialized"));
		fileChooser.setCurrentDirectory(new File("."));
		
		java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
        	if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
        	
        		new StartingPointTester01(fileChooser.getSelectedFile());

            }
        }
        });
		

	}

}
