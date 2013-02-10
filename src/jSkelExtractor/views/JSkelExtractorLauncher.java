package jSkelExtractor.views;

import jSkelExtractor.algorithms.IComposedAlgorithm;
import jSkelExtractor.algorithms.Skeletrizer;
import jSkelExtractor.algorithms.sdskeletrizer.SDSkeletrizer;
import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.controllers.SkeletrizeController;
import jSkelExtractor.utils.MeshIO;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;


public class JSkelExtractorLauncher extends JFrame{

	private static final long serialVersionUID = 931946924323213823L;
	protected static JFileChooser fileChooser;
	
	protected SkeletrizeController controller;
	protected JFrame algoFrame;	
	protected JFrame graphFrame;
	protected IComposedAlgorithm skeletrizer;
	
	public JSkelExtractorLauncher(File serializedMesh) throws InterruptedException, InvocationTargetException {
		final File mesh = serializedMesh;
		
		
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
	
				skeletrizer = new SDSkeletrizer(MeshIO.importSerialized(mesh));
			}
        });

		while(skeletrizer==null){
			Thread.sleep(500);
		}
		
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				MeshPanel algorithmView;
				String algorithmViewTitle = "AlgorithmView";    	
				algorithmView = new MeshPanel(ConfigObj.getInstance().getResContainer(),skeletrizer,algorithmViewTitle);
				algorithmView.setNODE_RADIUS(0.2);
				algorithmView.magnification = algorithmView.magnification + 0.5;
				algoFrame = new JFrame(algorithmViewTitle);
				algoFrame.getContentPane().add(algorithmView.getWindowPanel());
				algoFrame.pack();
			}
        });

		
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				GraphPanel graphView;
				String graphViewTitle = "GraphView";
				graphView = new GraphPanel(ConfigObj.getInstance().getResContainer(),graphViewTitle);
				graphView.setNODE_RADIUS(0.2);
				graphView.magnification = graphView.magnification + 0.5;
				graphFrame = new JFrame(graphViewTitle);
				graphFrame.getContentPane().add(graphView.getWindowPanel());
				graphFrame.pack();
			}
        });


		
		controller = new SkeletrizeController((Skeletrizer)skeletrizer);
		this.getContentPane().add(controller,BorderLayout.SOUTH);
		
		this.pack();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		

		while((algoFrame==null)||(graphFrame==null)){
			Thread.sleep(500);
		}
		// Adjust Layout
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getSize();
        
        frameSize.height = screenSize.height;
        frameSize.width = screenSize.width;
        
        this.setLocation(0,0);
        algoFrame.setLocation(0,this.getHeight()+10);
        graphFrame.setLocation(algoFrame.getWidth()+10, this.getHeight()+10);

		graphFrame.setVisible(true);
		algoFrame.setVisible(true);
		this.setVisible(true);
		
	}

	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		ChimeraSplashScreen css = new ChimeraSplashScreen();
		fileChooser = new JFileChooser("Select Ply Mesh");
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Serialized Mesh","serialized"));
		fileChooser.setCurrentDirectory(new File("."));
		
		while(!css.isReady()){
			Thread.sleep(100);
		}
			
        if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
        	
        	try {
				new JSkelExtractorLauncher(fileChooser.getSelectedFile());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          }	
	}

}
