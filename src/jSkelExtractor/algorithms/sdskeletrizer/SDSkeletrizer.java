package jSkelExtractor.algorithms.sdskeletrizer;

import jSkelExtractor.algorithms.Skeletrizer;
import jSkelExtractor.utils.MeshManipulator;
import soam.Mesh;



public class SDSkeletrizer extends Skeletrizer {

	
	public SDSkeletrizer(Mesh mesh){
		super(mesh);
		
		MeshManipulator.removeIsolatedVertexes(mesh);
		
		configObj.getResContainer().setMesh(mesh);
		
		FindMaxMin fmm = new FindMaxMin(mesh);
		//FindMaxMin_Complete fmm = new FindMaxMin_Complete(mesh);
		this.setAlgorithm(fmm);
		
		FindDistFunctionMaximum fdfm = new FindDistFunctionMaximum(mesh);
		this.setAlgorithm(fdfm);
		
		FilterMLM fmlm = new FilterMLM(mesh);
		this.setAlgorithm(fmlm);
		
		FindFeaturePoints_Luchino ffp = new FindFeaturePoints_Luchino(mesh);
		this.setAlgorithm(ffp);
		
		DetectMappingFunction dmf = new DetectMappingFunction(mesh);
		this.setAlgorithm(dmf);
		
		DiscreteContoursGraph dcg = new DiscreteContoursGraph(mesh,true);
		this.setAlgorithm(dcg);
		

	//	MeshGraph d = configObj.getResContainer().getGraph();
//		d.vertexlist = mesh.vertexlist;
	}


	@Override
	public void previousAlgorithmCompleted() {
		
	}







}
