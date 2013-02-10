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

package jSkelExtractor.views;

import jSkelExtractor.algorithms.IComposedAlgorithm;
import jSkelExtractor.algorithms.sdskeletrizer.SDSResContainer;
import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.configs.ViewConfigObj;
import jSkelExtractor.utils.GraphicsFileExport;
import jSkelExtractor.utils.Transformation3D;
import jSkelExtractor.views.colorizers.IColorizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import soam.Edge;
import soam.Mesh;
import soam.Vertex;

/**
 * The 3d Visualization
 * 
 * 
 * @author Marco Piastra, Niccolo' Piarulli, Luca Bianchi
 * 
 */
public class MeshPanel extends JPanel implements MouseListener,
		MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private static final int SORT_IDX = 2;

	private static final BasicStroke defaultStroke = new BasicStroke(0.1f);
	private static final Color defaultColor = Color.DARK_GRAY;

	protected static String defaultPath = System.getProperty("user.dir");

	protected JPanel thisFrame;

	protected boolean SHOW_VERTICES = false;
	protected boolean SHOW_EDGES = false;
	protected boolean SHOW_EDGE_AGE = false;
	protected boolean SHOW_VERTEX_ERROR = false;

	protected boolean SHOW_POINT_CLOUD = false;
	protected boolean SHOW_INSERTION_THRESHOLDS = false;
	protected boolean SHOW_SAMPLED_POINTS = false;

	protected boolean SORT_VERTICES = true;

	protected boolean COLLECT_SAMPLED_POINTS = false;

	protected int DIM_X = 0;
	protected int DIM_Y = 1;
	protected int DIM_Z = 2;

	protected double FROM_X = 0;
	protected double FROM_Y = 0;
	protected double FROM_Z = 0;

	protected double TO_X = 0;
	protected double TO_Y = 0;
	protected double TO_Z = 0;

	protected double SHIFT_X = 0;
	protected double SHIFT_Y = 0;

	protected double PROJECTION_X = 0;
	protected double PROJECTION_Y = 0;

	protected double NODE_RADIUS = 0;

	protected ArrayList<double[]> sampledPoints = new ArrayList<double[]>();

	protected double alpha = 0;
	protected double beta = 0;
	protected double gamma = 0;

	protected double ANGLE_ALPHA = 0;
	protected double ANGLE_BETA = 0;
	protected double ANGLE_GAMMA = 0;

	int prevx = 0;
	int prevy = 0;

	double hvec[] = null;
	double hedge[] = null;

	Transformation3D rotMatrix = null;
	Transformation3D rotDelta = null;

	public double magnification = 1;

	double max[] = new double[3];

	double pXY1 = 0.5d;
	double pXY2 = 0.25;

	int netWidth = 0;
	int netHeight = 0;

	int error = 0;

	double normalize = 256;

	protected String description = "";

	/**
	 * The image from the SourceController
	 */
	protected BufferedImage img;

	/**
	 * The buffer strategy to avoid flickering
	 */
	protected BufferStrategy bufferStrategy;

	protected double maxEdgeError = Double.MIN_VALUE;
	protected double maxVertexError = Double.MIN_VALUE;

	private SDSResContainer resContainer;

	private ViewConfigObj viewConfigObj;

	private int height;

	private int width;

	/**
	 * Quick sort of an array
	 */
	public static void quickSort(double array[][], int[] idx) {
		quickSort(array, idx, 0, array.length - 1);
	}

	private static void quickSort(double array[][], int[] idx, int start,
			int end) {
		int i = start;
		int k = end;

		if (end - start >= 1) {
			double pivot = array[start][SORT_IDX];

			while (k > i) {
				while (array[i][SORT_IDX] >= pivot && i <= end && k > i)
					i++;
				while (array[k][SORT_IDX] < pivot && k >= start && k >= i)
					k--;
				if (k > i) {
					swap(array, idx, i, k);
				}
			}
			swap(array, idx, start, k);

			quickSort(array, idx, start, k - 1);
			quickSort(array, idx, k + 1, end);
		} else {
			return;
		}
	}

	private static void swap(double array[][], int[] idx, int index1, int index2) {
		double tempd = 0;
		int tempi = 0;

		for (int i = 0; i < array[index1].length; i++) {
			tempd = array[index1][i];
			array[index1][i] = array[index2][i];
			array[index2][i] = tempd;
		}

		tempi = idx[index1];
		idx[index1] = idx[index2];
		idx[index2] = tempi;
	}

	/**
	 * The constructor, set all the projection parameters and call the draw
	 * method
	 * 
	 * @param algorithm
	 *            the net
	 */
	public MeshPanel(SDSResContainer resContainer,
			IComposedAlgorithm algorithm, String description) {

		this.resContainer = resContainer;
		this.viewConfigObj = ViewConfigObj.getInstance();

		this.description = description;
		this.thisFrame = null;

		SHIFT_X = -0.5D;
		SHIFT_Y = -0.5D;

		rotMatrix = new Transformation3D();
		rotDelta = new Transformation3D();

		SHOW_EDGES = true;
		SHOW_VERTICES = true;

		setScaleParams(0d, 0d, normalize, normalize, 0.1d);

		set3DParams(0d, normalize, 0.9d, 0.1d);
		setEulerAngles(ANGLE_ALPHA, ANGLE_BETA, ANGLE_GAMMA);

		hvec = new double[3];
		hedge = new double[3];

		addMouseListener(this);
		addMouseMotionListener(this);

		setPreferredSize(new Dimension(400, 400));

		Timer t = new Timer(200, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();

			}

		});

		t.start();

	}

	public void setDescription(String text) {
		description = text;
	}

	@Override
	public void paint(Graphics plainG) throws IndexOutOfBoundsException {

		Graphics2D g = (Graphics2D) plainG;
		g.setStroke(defaultStroke);

		height = super.getSize().height;
		width = super.getSize().width;

		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);


		double pt[] = new double[3];

		if (SHOW_SAMPLED_POINTS) {

			g.setColor(Color.black);
			for (double[] sampledPoint : sampledPoints) {
				pt[0] = (sampledPoint[DIM_X] - FROM_X) / (TO_X - FROM_X);
				pt[1] = (sampledPoint[DIM_Y] - FROM_Y) / (TO_Y - FROM_Y);
				pt[2] = (sampledPoint[DIM_Z] - FROM_Z) / (TO_Z - FROM_Z);

				projectXY(pt);

				int l0 = (int) (hvec[0] * height + (width - height) * 0.5);
				int i0 = (int) (hvec[1] * height);

				g.fillOval(l0, i0, 2, 2);
			}
		}
		
		IColorizer colorizer = viewConfigObj.getAlgorithmColorizer();		
		this.drawMesh(g, ConfigObj.getInstance().getResContainer().getMesh(),colorizer);

	}

	private void drawMesh(Graphics2D g, Mesh mesh, IColorizer colorizer){

		Edge edge;
		Vertex vertex;
		
		int vertexSize = mesh.vertexlist.size();
		int edgeSize = mesh.edgelist.size();


		double vd[][] = new double[vertexSize][3];
		double vd1[] = new double[3];
		double eds[][] = new double[edgeSize][3];
		double eds1[] = new double[3];
		double ede[][] = new double[edgeSize][3];
		double ede1[] = new double[3];

		int[] idx = new int[vertexSize];

		if (SHOW_EDGES) {
			g.setColor(defaultColor);

			edge = null;
			double maxEdgeErrorThisTime = Double.MIN_VALUE;

			for (int i = 0; i < edgeSize; i++) {
				try {
					edge = mesh.edgelist.get(i);
					eds1[0] = (edge.start.position[DIM_X] - FROM_X)
							/ (TO_X - FROM_X);
					eds1[1] = (edge.start.position[DIM_Y] - FROM_Y)
							/ (TO_Y - FROM_Y);
					eds1[2] = (edge.start.position[DIM_Z] - FROM_Z)
							/ (TO_Z - FROM_Z);

					ede1[0] = (edge.end.position[DIM_X] - FROM_X)
							/ (TO_X - FROM_X);
					ede1[1] = (edge.end.position[DIM_Y] - FROM_Y)
							/ (TO_Y - FROM_Y);
					ede1[2] = (edge.end.position[DIM_Z] - FROM_Z)
							/ (TO_Z - FROM_Z);

				} catch (IndexOutOfBoundsException e) {
					error++;
				} catch (NullPointerException e) {
					error++;
				}

				projectXY(eds1);
				projectXYedge(ede1);

				for (int l3 = 0; l3 < 3; l3++) {
					eds[i][l3] = hvec[l3];
					ede[i][l3] = hedge[l3];
				}

				eds[i][0] = eds[i][0] * height + (width - height) * 0.5;
				eds[i][1] = eds[i][1] * height;

				ede[i][0] = ede[i][0] * height + (width - height) * 0.5;
				ede[i][1] = ede[i][1] * height;

				int l4 = (int) (eds[i][0]);
				int j4 = (int) (eds[i][1]);
				int k4 = (int) (ede[i][0]);
				int i4 = (int) (ede[i][1]);

				g.drawLine(l4, j4, k4, i4);
			}
			maxEdgeError = maxEdgeErrorThisTime;
		}

		if (SHOW_VERTICES) {
			double maxVertexErrorThisTime = Double.MIN_VALUE;

			try {
				for (int i = 0; i < vertexSize; i++) {

					vertex = mesh.vertexlist.get(i);

					idx[i] = i;

					vd1[0] = (vertex.position[DIM_X] - FROM_X)
							/ (TO_X - FROM_X);
					vd1[1] = (vertex.position[DIM_Y] - FROM_Y)
							/ (TO_Y - FROM_Y);
					vd1[2] = (vertex.position[DIM_Z] - FROM_Z)
							/ (TO_Z - FROM_Z);

					projectXY(vd1);

					for (int j = 0; j < 3; j++) {
						vd[i][j] = hvec[j];
					}

					vd[i][0] = vd[i][0] * height + (width - height) * 0.5;
					vd[i][1] = vd[i][1] * height;
				}

				if (SORT_VERTICES) {
					quickSort(vd, idx);
				}

				for (int i = 0; i < vertexSize; i++) {

					vertex = mesh.vertexlist.get(idx[i]);

					int l6 = (int) vd[i][0];
					int i7 = (int) vd[i][1];
					double d1 = PROJECTION_X + PROJECTION_Y + vd[i][2];

					int d = Math
							.round((int) ((30 * (NODE_RADIUS / (TO_X - FROM_X)) * height) / d1));
					d = Math.max(d, 2);

					l6 -= (d / 2);
					i7 -= (d / 2);

					g.setColor(defaultColor);
					g.drawOval(l6, i7, d, d);

					if (SHOW_VERTEX_ERROR) {
						if (vertex.error > maxVertexErrorThisTime) {
							maxVertexErrorThisTime = vertex.error;
						}
						int error = (int) (Math.min(vertex.error
								/ maxVertexError, 1) * 255);
						g.setColor(new Color(error, 0, 255 - error));
					} else {
						g.setColor(colorizer.getColorFor(vertex));
					}
					g.fillOval(l6, i7, d, d);

					if (SHOW_INSERTION_THRESHOLDS) {
						d = Math
								.round((int) (((vertex.insertionThreshold / (TO_X - FROM_X)) * height) / d1));
						d *= magnification;
						d = Math.max(d, 2);

						l6 = (int) vd[i][0] - (d / 2);
						i7 = (int) vd[i][1] - (d / 2);

						g.drawOval(l6, i7, d, d);
					}
				}
			} catch (IndexOutOfBoundsException e) {
				error++;
			} catch (NullPointerException e) {
				error++;
			}

			maxVertexError = maxVertexErrorThisTime;
		}

	}
	

	
	/**
	 * Set the scale parameters, for the z projection
	 * 
	 * @param d
	 *            z base
	 * @param d1
	 *            z displacement
	 * @param d2
	 *            x projection coefficient
	 * @param d3
	 *            y projection coefficient
	 */
	public void set3DParams(double d, double d1, double d2, double d3) {
		FROM_Z = d;
		TO_Z = d1;
		PROJECTION_X = d2;
		PROJECTION_Y = d3;
	}

	/**
	 * Set the scale parameters, for the x-y projection
	 * 
	 * @param d
	 *            x base
	 * @param d1
	 *            y base
	 * @param d2
	 *            x displacement
	 * @param d3
	 *            y displacement
	 * @param d4
	 *            node radius
	 */
	public void setScaleParams(double d, double d1, double d2, double d3,
			double d4) {
		FROM_X = d;
		FROM_Y = d1;
		TO_X = d2;
		TO_Y = d3;
		NODE_RADIUS = d4;
	}

	/**
	 * Calculate the rotation matrix
	 */
	public void calcRotMatrix() {

		rotMatrix.unit();
		rotMatrix.xRotate(alpha);
		rotMatrix.yRotate(beta);
		rotMatrix.zRotate(gamma);
	}

	/**
	 * Set the Euler angles for the 3-dimensional rotation, controlled by the
	 * mouse, and call calcRotMatrix()
	 * 
	 * @param a
	 *            y-mouse axis movement
	 * @param b
	 *            not used
	 * @param c
	 *            x-mouse axis movement
	 */
	public void setEulerAngles(double a, double b, double c) {
		boolean flag = false;

		if (a != alpha) {
			alpha = a;
			flag = true;
		}

		if (b != beta) {
			beta = b;
			flag = true;
		}

		if (c != gamma) {
			gamma = c;
			flag = true;
		}

		if (flag)
			calcRotMatrix();
	}

	/**
	 * Reset the Euler angles, as computed from rotMatrix
	 */
	public void resetEulerAngles() {
		alpha = rotMatrix.getAlpha();
		beta = rotMatrix.getBeta();
		gamma = rotMatrix.getGamma();

		ANGLE_ALPHA = alpha;
		ANGLE_BETA = beta;
		ANGLE_GAMMA = gamma;
	}

	/**
	 * This method is used to project 3D points over a 2D canvas
	 * 
	 * Used for vertex & edge.start
	 * 
	 * @param ad
	 *            the vertex coordinates
	 */
	public void projectXY(double ad[]) {
		double ad1[] = new double[3];
		double ad2[] = new double[3];

		ad2[0] = ad[0] - 0.5;
		ad2[1] = ad[1] - 0.5;
		ad2[2] = ad[2] - 0.5;

		rotMatrix.transform(ad2, ad1);

		ad1[0] += 0.5;
		ad1[1] += 0.5;
		ad1[2] += 0.5;

		hvec[0] = ((ad1[0] + SHIFT_X) * PROJECTION_X * magnification)
				/ (PROJECTION_X + PROJECTION_Y + ad1[2]) + 0.5;
		hvec[1] = ((ad1[1] + SHIFT_Y) * PROJECTION_X * magnification)
				/ (PROJECTION_X + PROJECTION_Y + ad1[2]) + 0.5;
		hvec[2] = ad1[2];
	}

	/**
	 * This method is used to project 3D points over a 2D canvas
	 * 
	 * Used for edge.end
	 * 
	 * @paramad the vertex coordinates
	 */
	public void projectXYedge(double ad[]) {
		double ad1[] = new double[3];
		double ad2[] = new double[3];

		ad2[0] = ad[0] - 0.5;
		ad2[1] = ad[1] - 0.5;
		ad2[2] = ad[2] - 0.5;

		rotMatrix.transform(ad2, ad1);

		ad1[0] += 0.5;
		ad1[1] += 0.5;
		ad1[2] += 0.5;

		hedge[0] = ((ad1[0] + SHIFT_X) * PROJECTION_X * magnification)
				/ (PROJECTION_X + PROJECTION_Y + ad1[2]) + 0.5;
		hedge[1] = ((ad1[1] + SHIFT_Y) * PROJECTION_X * magnification)
				/ (PROJECTION_X + PROJECTION_Y + ad1[2]) + 0.5;
		hedge[2] = ad1[2];
	}

	/**
	 * The zoom method, sx clic => more zoom dx click => less zoom
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * Set the x-y coordinates before the drag action
	 */
	public void mousePressed(MouseEvent e) {
		prevx = e.getX();
		prevy = e.getY();
		e.consume();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	/**
	 * On mouse dragging this method sets the Euler angles to implement the
	 * rotation
	 */
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		if (e.isControlDown()) {
			double dy = (prevy - y) * (1d / (200 * magnification));
			double dx = (x - prevx) * (1d / (200 * magnification));

			SHIFT_X += dx;
			SHIFT_Y -= dy;

			repaint();
		} else {
			double dBeta = (prevy - y) * (1d / 80);
			double dAlpha = (x - prevx) * (1d / 80);

			rotDelta.unit();
			rotDelta.yRotate(-dAlpha);
			rotDelta.xRotate(-dBeta);

			rotMatrix.multiply(rotDelta);

			resetEulerAngles();

			repaint();
		}

		prevx = x;
		prevy = y;
		e.consume();
	}

	public void mouseMoved(MouseEvent e) {
	}

	public JPanel getWindowPanel() {
		if (thisFrame == null) {
			thisFrame = new Mesh3dWindow(this);
		}
		return thisFrame;
	}

	protected class Mesh3dWindow extends JPanel {
		private static final long serialVersionUID = 1L;

		private final static String PLUS_ICON_LOCATION = "icons/plus-8.png";
		private final static String MINUS_ICON_LOCATION = "icons/minus-8.png";

		protected final static double MAX_ZOOM = 128d;
		protected final static double MIN_ZOOM = 0.5d;

		protected MeshPanel view3dPanel;

		protected JButton optionsMenuItem;
		protected JButton exportPNGMenuItem;
		protected JButton exportEPSMenuItem;

		protected JButton increaseButton;
		protected JButton decreaseButton;

		protected BeanEditor optionsEditor;

		protected Mesh3dWindow(MeshPanel panel) {
			super();
			this.view3dPanel = panel;

			// Horizontal menu
			Box menuBar = Box.createHorizontalBox();
			// JMenu menu = new JMenu("View Menu");
			exportPNGMenuItem = new JButton("Export as PNG");
			exportEPSMenuItem = new JButton("Export as EPS");
			optionsMenuItem = new JButton("View Options");

			ActionListener menuListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == optionsMenuItem) {
						if (optionsEditor == null) {
							optionsEditor = new BeanEditor(view3dPanel);
							optionsEditor.setLocation(1170,95);


						}
						optionsEditor.setVisible(true);
					} else if (e.getSource() == exportPNGMenuItem) {
						while (true) {
							JFileChooser fc = new JFileChooser(defaultPath);
							fc
									.addChoosableFileFilter(new FileNameExtensionFilter(
											"PNG Files", "png"));
							fc.setDialogTitle(this.getClass().getName()
									+ " : Export to PNG");

							if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
								File file = fc.getSelectedFile();
								defaultPath = file.getAbsolutePath();

								if (!GraphicsFileExport.exportPNG(view3dPanel,
										file, view3dPanel.getWidth(),
										view3dPanel.getWidth())) {

									JOptionPane.showMessageDialog(null,
											"Could not export to \""
													+ file.getPath() + "\"",
											"Error", JOptionPane.ERROR_MESSAGE);
									continue;
								}
								break;
							} else {
								break;
							}
						}
					} else if (e.getSource() == exportEPSMenuItem) {
						while (true) {
							JFileChooser fc = new JFileChooser(defaultPath);
							fc
									.addChoosableFileFilter(new FileNameExtensionFilter(
											"EPS Files", "eps"));
							fc.setDialogTitle(this.getClass().getName()
									+ " : Export to EPS");

							if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
								File file = fc.getSelectedFile();
								defaultPath = file.getAbsolutePath();

								if (!GraphicsFileExport.exportEPS(view3dPanel,
										file, view3dPanel.getWidth(),
										view3dPanel.getWidth())) {

									JOptionPane.showMessageDialog(null,
											"Could not export to \""
													+ file.getPath() + "\"",
											"Error", JOptionPane.ERROR_MESSAGE);
									continue;
								}
								break;
							} else {
								break;
							}
						}
					}
				}
			};

			exportPNGMenuItem.addActionListener(menuListener);
			exportEPSMenuItem.addActionListener(menuListener);
			optionsMenuItem.addActionListener(menuListener);

			menuBar.add(exportPNGMenuItem);
			menuBar.add(exportEPSMenuItem);
			// menuBar.addSeparator();
			menuBar.add(optionsMenuItem);
			// menuBar.add(menu);

			// Vertical toolbar
			JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);

			increaseButton = new JButton();
			increaseButton.setIcon(new ImageIcon(this.getClass().getResource(
					PLUS_ICON_LOCATION)));

			decreaseButton = new JButton();
			decreaseButton.setIcon(new ImageIcon(this.getClass().getResource(
					MINUS_ICON_LOCATION)));

			ActionListener toolBarListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == increaseButton) {
						view3dPanel.magnification = Math.min(MAX_ZOOM,
								view3dPanel.magnification + 0.5);
						view3dPanel.repaint();
					} else if (e.getSource() == decreaseButton) {
						view3dPanel.magnification = Math.max(MIN_ZOOM,
								view3dPanel.magnification - 0.5);
						view3dPanel.repaint();
					}
				}
			};

			increaseButton.addActionListener(toolBarListener);
			decreaseButton.addActionListener(toolBarListener);

			toolBar.add(increaseButton);
			toolBar.add(decreaseButton);

			Box viewOptionPanel = buldViewOptionPane();

			GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);

			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(menuBar, GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE).addGroup(
							layout.createParallelGroup().addComponent(
									viewOptionPanel).addComponent(toolBar)
									.addComponent(view3dPanel)));
			layout.setHorizontalGroup(layout.createParallelGroup()
					.addComponent(menuBar).addGroup(
							layout.createSequentialGroup().addComponent(
									viewOptionPanel)
									.addComponent(toolBar).addComponent(
											view3dPanel)));
			Box appContainer = Box.createVerticalBox();
			appContainer.add(menuBar);

		}

		private Box buldViewOptionPane() {
			Box panel = Box.createVerticalBox();
			panel.add(new JLabel("View Options"));

			final ViewConfigObj vco = ViewConfigObj.getInstance();
			// Set<Map.Entry<String, Boolean>> properties = vco.entrySet();
			JCheckBox check;
			for (int i = 0; i < vco.size(); i++) {
				Map.Entry<String, Boolean> prop = vco.getIndexedKey(i);
				check = new JCheckBox(prop.getKey(), prop.getValue());
				check.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						boolean value = (e.getStateChange() == ItemEvent.SELECTED);
						vco.put(((JCheckBox) e.getItem()).getText(), value);
					}
				});
				panel.add(check);
			}
			return panel;
		}

		@Override
		public void repaint() {
			super.repaint();

		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);

			if (!visible && optionsEditor != null) {
				optionsEditor.setVisible(visible);
			}
		}

		/*
		 * @Override public void dispose() { if (optionsEditor != null) {
		 * optionsEditor.dispose(); }
		 * 
		 * super.dispose(); }
		 */

	}

	public boolean isSHOW_VERTICES() {
		return SHOW_VERTICES;
	}

	public void setSHOW_VERTICES(boolean show_vertices) {
		SHOW_VERTICES = show_vertices;
	}

	public boolean getSHOW_EDGES() {
		return SHOW_EDGES;
	}

	public void setSHOW_EDGES(boolean show_edges) {
		SHOW_EDGES = show_edges;
	}

	public boolean getSHOW_EDGE_AGE() {
		return SHOW_EDGE_AGE;
	}

	public void setSHOW_EDGE_AGE(boolean show_edge_age) {
		SHOW_EDGE_AGE = show_edge_age;
	}

	public double getFROM_X() {
		return FROM_X;
	}

	public void setFROM_X(double from_x) {
		FROM_X = from_x;
	}

	public double getFROM_Y() {
		return FROM_Y;
	}

	public void setFROM_Y(double from_y) {
		FROM_Y = from_y;
	}

	public double getFROM_Z() {
		return FROM_Z;
	}

	public void setFROM_Z(double from_z) {
		FROM_Z = from_z;
	}

	public double getTO_X() {
		return TO_X;
	}

	public void setTO_X(double to_x) {
		TO_X = to_x;
	}

	public double getTO_Y() {
		return TO_Y;
	}

	public void setTO_Y(double to_y) {
		TO_Y = to_y;
	}

	public double getTO_Z() {
		return TO_Z;
	}

	public void setTO_Z(double to_z) {
		TO_Z = to_z;
	}

	public double getSHIFT_X() {
		return SHIFT_X;
	}

	public void setSHIFT_X(double shift_x) {
		SHIFT_X = shift_x;
	}

	public double getSHIFT_Y() {
		return SHIFT_Y;
	}

	public void setSHIFT_Y(double shift_y) {
		SHIFT_Y = shift_y;
	}

	public double getPROJECTION_X() {
		return PROJECTION_X;
	}

	public void setPROJECTION_X(double projection_x) {
		PROJECTION_X = projection_x;
	}

	public double getPROJECTION_Y() {
		return PROJECTION_Y;
	}

	public void setPROJECTION_Y(double projection_y) {
		PROJECTION_Y = projection_y;
	}

	public double getNODE_RADIUS() {
		return NODE_RADIUS;
	}

	public void setNODE_RADIUS(double node_radius) {
		NODE_RADIUS = node_radius;
	}

	public int getDIM_X() {
		return DIM_X;
	}

	public void setDIM_X(int dim_x) {
		DIM_X = dim_x;
	}

	public int getDIM_Y() {
		return DIM_Y;
	}

	public void setDIM_Y(int dim_y) {
		DIM_Y = dim_y;
	}

	public int getDIM_Z() {
		return DIM_Z;
	}

	public void setDIM_Z(int dim_z) {
		DIM_Z = dim_z;
	}

	public boolean isSHOW_SAMPLED_POINTS() {
		return SHOW_SAMPLED_POINTS;
	}

	public void setSHOW_SAMPLED_POINTS(boolean show_sampled_points) {
		SHOW_SAMPLED_POINTS = show_sampled_points;
	}

	public boolean isCOLLECT_SAMPLED_POINTS() {
		return COLLECT_SAMPLED_POINTS;
	}

	public void setCOLLECT_SAMPLED_POINTS(boolean collect_sampled_points) {
		COLLECT_SAMPLED_POINTS = collect_sampled_points;

		if (!COLLECT_SAMPLED_POINTS) {
			sampledPoints.clear();
		}
	}

	public double getANGLE_ALPHA() {
		return ANGLE_ALPHA;
	}

	public void setANGLE_ALPHA(double angle_alpha) {
		ANGLE_ALPHA = angle_alpha;
		setEulerAngles(ANGLE_ALPHA, ANGLE_BETA, ANGLE_GAMMA);
	}

	public double getANGLE_BETA() {
		return ANGLE_BETA;
	}

	public void setANGLE_BETA(double angle_beta) {
		ANGLE_BETA = angle_beta;
		setEulerAngles(ANGLE_ALPHA, ANGLE_BETA, ANGLE_GAMMA);
	}

	public double getANGLE_GAMMA() {
		return ANGLE_GAMMA;
	}

	public void setANGLE_GAMMA(double angle_gamma) {
		ANGLE_GAMMA = angle_gamma;
		setEulerAngles(ANGLE_ALPHA, ANGLE_BETA, ANGLE_GAMMA);
	}

	public boolean isSORT_VERTICES() {
		return SORT_VERTICES;
	}

	public void setSORT_VERTICES(boolean sort_vertices) {
		SORT_VERTICES = sort_vertices;
	}

	public boolean isSHOW_INSERTION_THRESHOLDS() {
		return SHOW_INSERTION_THRESHOLDS;
	}

	public void setSHOW_INSERTION_THRESHOLDS(boolean show_insertion_thresholds) {
		SHOW_INSERTION_THRESHOLDS = show_insertion_thresholds;
	}

	public boolean isSHOW_VERTEX_ERROR() {
		return SHOW_VERTEX_ERROR;
	}

	public void setSHOW_VERTEX_ERROR(boolean show_vertex_error) {
		SHOW_VERTEX_ERROR = show_vertex_error;
	}

	public boolean isSHOW_POINT_CLOUD() {
		return SHOW_POINT_CLOUD;
	}

	public void setSHOW_POINT_CLOUD(boolean show_point_cloud) {
		SHOW_POINT_CLOUD = show_point_cloud;
	}

}
