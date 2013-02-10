package jSkelExtractor.views;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class ChimeraSplashScreen extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean ready;
	private JLabel loadingLabel;
	
	public ChimeraSplashScreen(){
		ready = false;
		loadingLabel = new JLabel();
		try {
			File imageFile = new File(System.getProperty("user.dir")+"/bin/jSkelExtractor/"+"ChimeraSplash.jpg");
			this.getContentPane().add(new JLabel(new ImageIcon(ImageIO.read(imageFile))),BorderLayout.CENTER);
			setLoading("applcation");
			this.getContentPane().add(loadingLabel,BorderLayout.SOUTH);
			this.setTitle("The Chimera Project");
			this.pack();
			this.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2-this.getWidth()/2,Toolkit.getDefaultToolkit().getScreenSize().height/2-this.getHeight()/2);
			this.setVisible(true);
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			Timer t = new Timer(4000,new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					setReady();
					setVisible(false);
					dispose();
				}
				
			});
			t.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setLoading(String string) {
		loadingLabel.setText("Loading "+string+"...");
		
	}

	private void setReady(){
		this.ready = true;
	}

	public boolean isReady() {
		return this.ready;
	}

}
