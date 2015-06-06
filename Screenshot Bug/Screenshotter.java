import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.*;

import javax.imageio.ImageIO;

import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class Screenshotter implements Runnable {
	/**
	 * Formatter for timestamps.
	 */
	private SimpleDateFormat formatter = new SimpleDateFormat("MM.dd.yyyy hh.mm.ss a");
	/**
	 * The client ID used to upload to imgur. This comes from the file "client_id.txt"
	 */
	private String CLIENT_ID;
	/**
	 * Time (seconds) between screenshots
	 */
	private final int DELAY_TIME = 10;
	/**
	 * Number of screenshots to save internally before uploading to an album, and
	 * wiping internal storage.
	 */
	private final int ALBUM_SIZE = 20;
	/**
	 * The container for the images pending upload to the album.
	 * Is cleared once its size exceeds ALBUM_SIZE.
	 */
	private ArrayList<String> uploadedImages = new ArrayList<String>();
	/**
	 * The timestamp to be used in album or file creation.
	 */
	private String stamp;

	/**
	 * Save the image to disk if true. Otherwise, will upload to imgur.
	 */
	private boolean SAVE = false;

	/**
	 * Load the client ID to use with Imgur from the file "client_id.txt" in the 
	 * relative home directory.
	 */
	public Screenshotter() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("client_id.txt"));
			CLIENT_ID = br.readLine().trim();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Takes the screenshot, saving a snapshot of the time as well. Saves it to
	 * a file if requested.
	 * @return
	 */
	public BufferedImage takeScreenshot() {
		try {
			Calendar cal = Calendar.getInstance();

			BufferedImage currentImage = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			stamp = formatter.format(cal.getTime());
			if (SAVE) {
				ImageIO.write(currentImage, "png", new File(stamp + ".png"));
				System.out.println("Saved: " + stamp);
			}

			return currentImage;
		} catch (HeadlessException | AWTException | IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String startStamp;

	@Override
	public void run() {
		while (true) {
			BufferedImage b = takeScreenshot();

			try {
				if (!SAVE) {
					Imgur i = new Imgur(CLIENT_ID);
					String response = i.uploadImage(imageToBase64(b));
					//System.out.println(response);

					JSONObject o = i.parseJSON(response);
					String id = o.getString("id"); //Gets the imgur ID
					uploadedImages.add(id); //Add to collection
					int progress = uploadedImages.size();
					System.out.println("Added " + id + ", size: " + progress);

					//Saves starting time
					if (progress == 1) {
						startStamp = stamp;
					}

					//Album creation check
					if (progress == ALBUM_SIZE) {
						String response2 = i.createNewAlbum(uploadedImages.toArray(new String[uploadedImages.size()]),
								startStamp + " - " + stamp); //Pretty title
						JSONObject o2 = new JSONObject(response2);
						JSONObject o3 = o2.optJSONObject("data");

						String id2 = o3.getString("id");
						System.out.println();
						String albumLink = "imgur.com/a/" + id2;
						System.out.println(albumLink);

						//Send link to address
						Mailer m = new Mailer("reedtrevelyan@gmail.com",  startStamp + " - " + stamp);
						/* Yahoo mail servers don't like emails with only a link
						 * in the body. So we add some text to our link.*/
						m.sendMail("Here's the link ^^ : " + albumLink, true);

						uploadedImages.clear();
					}
				}
			} catch (Exception e1) {
				System.err.print("Error in run()");
				e1.printStackTrace();
			}

			//Halt loop
			try {
				Thread.sleep(DELAY_TIME * 1000); //to ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Converts a BufferedImage into a Base64 string.
	 * @param i The bufferedImage.
	 * @return A Base64 string representation
	 * @throws IOException If the image cannot be written to the output stream.
	 */
	private String imageToBase64(BufferedImage i) throws IOException {
		BufferedImage image = i;
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		ImageIO.write(image, "png", byteArray);
		byte[] byteImage = byteArray.toByteArray();
		String dataImage = encodeImage(byteImage);

		return dataImage;
	}

	/**
	 * Uses sun BASE64Encoder to encode the string.
	 * @param imageByteArray
	 * @return
	 */
	private static String encodeImage(byte[] imageByteArray) {
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(imageByteArray);
	}

	/* private static byte[] decodeImage(String imageDataString) {
        return Base64.decodeBase64(imageDataString);
    }*/

	/**
	 * Test encoding/decoding
	 * @param Base64Image The encoded image
	 */
	/* private void test(byte[] Base64Image) {
		try {
			FileOutputStream imageOutFile = new FileOutputStream("after-convert.png");
			imageOutFile.write(Base64Image);
			imageOutFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} */
}
