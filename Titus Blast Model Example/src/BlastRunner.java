/*
 * BlastRunner.java
 * Author: Nathan Titus
 * Date: 4/22/2023
 * Purpose: Runs and animates an example simulation of an explosive blast 
 * using particle flux to represent the incident pressure wave.
 * 
 * Version Notes: I didn't have time to implement this the way I would like.
 * Preferably the blast physics would be handled in its own thread running 
 * asynchronously from the GUI. Then the GUI would simply render the most recent
 * state of the physics simulation. The physics could then be implemented 
 * using differential equations and time steps with meaningful units.
 */

import javax.swing.SwingUtilities;

//Calculates and animates particle blast model
public class BlastRunner {

	public static void main(String[] arg) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
					BlastGui window = new BlastGui();
					window.setVisible(true);
			}
		});
	}

}