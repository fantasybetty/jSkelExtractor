package jSkelExtractor.algorithms.sdskeletrizer;

import jSkelExtractor.algorithms.AbstractAlgorithm;
import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.controllers.DCGraphController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import soam.Mesh;
import soam.Vertex;

public class DiscreteContoursGraph extends AbstractAlgorithm {

	protected SDSResContainer res;

	protected ArrayList<Vertex> Vt;
	protected ArrayList<Vertex> Cd;

	private int n_gamma_v_t2;
	private int n_gamma_v_t1;

	private ArrayList<ConnectedComponent> connectedComponents;

	private ArrayList<ArrayList<Vertex>> gamma_v_t1;
	private ArrayList<ArrayList<Vertex>> gamma_v_t2;

	private MeshGraph graph;

	private int iteration;

	private boolean interactive;

	private boolean finished;

	public DiscreteContoursGraph(Mesh mesh, boolean interactive) {
		super(mesh, ConfigObj.getInstance());
		this.res = configObj.getResContainer();
		this.interactive = interactive;
		if (this.interactive) {
			new DCGraphController(this);
			finished = false;
		}
		this.reset();
	}

	public DiscreteContoursGraph(Mesh mesh) {
		this(mesh, false);
	}

	@Override
	public void execute() {
		// this.reset();

		graph = res.getGraph();

		Map<Vertex, Double> df = res.getDistanceMap();
		for (Map.Entry<Vertex, Double> entry : df.entrySet()) {
			if (entry.getValue() == 0.0) {
				if (!Cd.contains(entry.getKey()))
					Cd.add(entry.getKey());
			}

		}

		connectedComponents = new ArrayList<ConnectedComponent>();
		n_gamma_v_t1 = 0;

		loopOnDiscreteContours();

		setChanged();
		notifyObservers();

	}

	private void loopOnDiscreteContours() {
		Vertex v = null;
		Double minValue = null;

		Map<Vertex, Double> df = res.getDistanceMap();
		while (Cd.size() != 0) {

			// v = argmin(Cd)
			minValue = null;
			for (Vertex vertex : Cd) {
				if ((minValue == null) || (minValue > df.get(vertex))) {
					v = vertex;
					minValue = df.get(vertex);

				}
			}

			// Cd <- Gamma(v)

			// gamma(v) = insieme dei vertici tra loro connessi
			gamma_v_t2 = findDiscreteContours(Cd);
			configObj.getResContainer().setDiscreteContoursList(gamma_v_t2);

			n_gamma_v_t2 = gamma_v_t2.size();

			// analisi dei contorni e costruzione grafo
			analizeContoursAndBuildGraph();

			// elimino v dall'insieme dei candidati
			Cd.remove(v);

			// aggiungo il vicinato di v non contenuto in Vt a Cd
			for (Vertex neighboor : v.getNeighbors()) {
				if ((!Vt.contains(neighboor)) && (!Cd.contains(neighboor)))
					Cd.add(neighboor);
			}

			// aggiungo v a Vt
			Vt.add(v);

			n_gamma_v_t1 = n_gamma_v_t2;
			gamma_v_t1 = gamma_v_t2;

			iteration++;

			if (interactive) {
				if (Cd.size() == 0)
					finished = true;
//				for (Vertex vCd : Cd) {
//					if (!graph.vertexlist.contains(vCd))
//						graph.vertexlist.add(vCd);
//				}

				break;
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void analizeContoursAndBuildGraph() {

		// analisi del numero di contorni esistenti

		ArrayList<ConnectedComponent> cc_tmp = null;
		ArrayList<ArrayList<Vertex>> gamma_tmp = null;

		/*
		 * 0. INIZIALIZZAZIONE Creo una copia di gamma_v (gamma_tmp) e di CC
		 * (cc_tmp) che uso come lista di appoggio per non perdere le
		 * informazioni. gamma_tmp è una copia esatta di gamma_v, contiene cioè
		 * i puntatori a tutti i contorni, così come cc_tmp lo è per i puntatori
		 * alle componenti connesse
		 */

		gamma_tmp = (ArrayList<ArrayList<Vertex>>) gamma_v_t2.clone();
		cc_tmp = (ArrayList<ConnectedComponent>) connectedComponents.clone();

		ArrayList<Vertex> currentDC = null;
		Iterator<ArrayList<Vertex>> gamma_it = gamma_tmp.iterator();

		ConnectedComponent currentCC = null;
		Iterator<ConnectedComponent> cc_it;

		boolean matching = false;
		ConnectedComponent matchingComponent = null;

		ArrayList<ConnectedComponent> alreadyMatchedCC = null;
		ArrayList<ArrayList<Vertex>> alreadyVisitedDC = new ArrayList<ArrayList<Vertex>>();

		while (gamma_it.hasNext()) {
			currentDC = gamma_it.next();

			cc_it = cc_tmp.iterator();
			alreadyMatchedCC = new ArrayList<ConnectedComponent>();
			while (cc_it.hasNext()) {
				matching = false;
				currentCC = cc_it.next();

				/*
				 * 1. VERIFICA MATCHING CON LISTA CC Il vicinato dei vertici di
				 * una lista di cc_tmp contiene TUTTI i punti della lita di
				 * gamma_tmp a cui è connessa, e questa operazione DEVE dare
				 * come risultato un match unico; in caso contrario (2
				 * corrispondenze oppure nessuna) non si ha matching.
				 */

				if (currentCC.contains(currentDC)) {
					if (!alreadyMatchedCC.contains(currentCC)
							&& (!alreadyVisitedDC.contains(currentDC))) {
						matching = true;
						matchingComponent = currentCC;
						alreadyMatchedCC.add(currentCC);
						alreadyVisitedDC.add(currentDC);
					} else {
						matching = false;
						matchingComponent = null;
						break;
					}
				}
			}

			if (matching) {
				/*
				 * 2. AGGIUNTA DC A CC Per le componenti connesse (CC) riferite
				 * a questi discreteContours (DC) non vi è alcun cambiamento. La
				 * lista di CC rimane aperta, solo i punti del DC non presenti
				 * vengono aggiunti.
				 */
				matchingComponent.addVertexes(currentDC);
				/*
				 * 3. RIMOZIONE DC DA LISTA
				 * 
				 * Una volta aggiunto il DC lo rimuovo dalla lista gamma_tmp, ed
				 * il suo match dalla lista cc_tmp. Al termine gamma_tmp e
				 * cc_tmp conterranno solo quei contorni e quelle cc per cui
				 * sono occorse delle variazioni tra t1 e t2. In particolar
				 * modo: - se n_gamma_v_t1<n_gamma_v_t2 allora BIFORCAZIONE - se
				 * n_gamma_v_t1>n_gamma_v_t2 allora UNIONE
				 */
				gamma_it.remove();
				cc_tmp.remove(matchingComponent);
			}

		}

		gamma_it = gamma_tmp.iterator();

		//TODO: System.out.println("Gamma originari: " + gamma_v_t2.size()+ " Gamma rimanenti: " + gamma_tmp.size());
		System.out.println("Gamma originari: " + gamma_v_t2.size() + " Gamma rimanenti: " + gamma_tmp.size());
		
		ConnectedComponent newComponent;
		while (gamma_it.hasNext()) {
			/*
			 * 4. APRO NUOVE CC Apro tante CC quanti sono i contorni di
			 * gamma_tmp rimanenti, a ciascuna aggiungo i vertici del rispettivo
			 * DC e per ciascuna inizializzo il vertice baricentro. Aggiungo le
			 * nuove CC alla lista di CC.
			 */
			currentDC = gamma_it.next();
			newComponent = new ConnectedComponent();
			newComponent.addVertexes(currentDC);

			/*
			 * 5. CHIUDO LE VECCHIE CC Aggiungo i nuovi baricentri delle CC
			 * appena create al grafo indicandoli come successors dei baricentri
			 * delle CC presenti in cc_tmp. In questo modo aggiorno il grafo.
			 * Per ogni vecchia CC calcolo anche il peso del baricentro (numero
			 * di vertici della CC) e lo aggiungo al grafo come valore di quel
			 * vertice nella matrice dei pesi. Chiudo le componenti connesse
			 * rimanenti in cc_tmp eliminandole dalla lista ConnectedComponents,
			 * tanto i vertici dei baricentri rimangono nel grafo
			 */
			cc_it = cc_tmp.iterator();
			ArrayList<Vertex> neighbohrs;
			while (cc_it.hasNext()) {
				neighbohrs = new ArrayList<Vertex>();
				currentCC = cc_it.next();
				for (Vertex ccVertex : currentCC.getVertexes())
					neighbohrs.addAll(ccVertex.getNeighbors());
				for (Vertex dcVertex : currentDC) {
					if (neighbohrs.contains(dcVertex)) {
						currentCC.addFollowers(newComponent.getBaricenter(),
								false);
						currentCC.close();
						graph.addNode(currentCC.getBaricenter());
						connectedComponents.remove(currentCC);
					}
				}
			}
			connectedComponents.add(newComponent);
			gamma_it.remove();
		}

		//TODO: Eliminare System.out.println("Graph has "+graph.vertexlist.size()+" vertexes");		
		System.out.println("Graph has "+graph.vertexlist.size()+" vertexes");
	}

	@SuppressWarnings("unchecked")
	private ArrayList<ArrayList<Vertex>> findDiscreteContours(
			ArrayList<Vertex> Cd) {
		ArrayList<ArrayList<Vertex>> contoursList = new ArrayList<ArrayList<Vertex>>();

		ArrayList<Vertex> tmp_cd = (ArrayList<Vertex>) Cd.clone(); 
		
		boolean createMoreContours = true;
		while(createMoreContours){
			createMoreContours = false;
			
			ArrayList<Vertex> newContour = new ArrayList<Vertex>();
			newContour.add(tmp_cd.get(0));
			tmp_cd.remove(0);
			
			boolean addMoreVertexes = true;
			Vertex vCd;
			Iterator<Vertex> cd_it;
			while(addMoreVertexes){
				addMoreVertexes = false;
				cd_it = tmp_cd.iterator();
				while(cd_it.hasNext()){
					vCd = cd_it.next();
					for(Vertex v:newContour){
						if((!newContour.contains(vCd))&&(v.getNeighbors().contains(vCd))){
							newContour.add(vCd);
							cd_it.remove();
							addMoreVertexes = true;
							break;
						}
					}
				}

			}
			contoursList.add(newContour);
			if(tmp_cd.size() != 0) createMoreContours = true;
		}
		
		//TODO: Eliminare System.out.println(contoursList.size());
		System.out.println("Detected "+contoursList.size()+" discrete contours");
		return contoursList;
	}

	public void next() {
		if (!finished)
			this.loopOnDiscreteContours();
	}

	@Override
	public void reset() {
		iteration = 0;
		Vt = new ArrayList<Vertex>();
		Cd = new ArrayList<Vertex>();
		n_gamma_v_t2 = 0;
		n_gamma_v_t1 = 0;
		gamma_v_t1 = new ArrayList<ArrayList<Vertex>>();
		gamma_v_t2 = new ArrayList<ArrayList<Vertex>>();
		connectedComponents = new ArrayList<ConnectedComponent>();
	}

	public boolean isInteractive() {
		return interactive;
	}

	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	public ArrayList<Vertex> getCd() {
		return this.Cd;
	}

}
