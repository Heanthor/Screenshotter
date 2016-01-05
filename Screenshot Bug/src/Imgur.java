package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

/**
 * Contains methods used to connect to imgur, upload an image or add images to an album.
 * @author Reed
 *
 */
public class Imgur {
	private String clientID;
	
	/**
	 * Default constructor
	 * @param clientID The ID to be used in authentication.
	 */
	public Imgur(String clientID) {
		this.clientID = clientID;
	}
	
	/**
	 * Uploads the encoded image to imgur. Receives the response and returns it.
	 * @param encodedImage Base64 encoded image
	 * @return JSON imgur response
	 * @throws Exception If connection goes wrong
	 */
	public String uploadImage(String encodedImage) throws Exception {
		URL url;
		url = new URL("https://api.imgur.com/3/image");
		HttpURLConnection conn = connect("POST", url);

		String data = URLEncoder.encode("image", "UTF-8") + "="
				+ URLEncoder.encode(encodedImage, "UTF-8");

		conn.connect();
		StringBuilder stb = new StringBuilder();
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();

		// Get the response
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			stb.append(line).append("\n");
		}
		wr.close();
		rd.close();

		return stb.toString();
	}

	/**
	 * Create anonymous imgur album, with the images passed as an argument.
	 * @param ids Array of imgur IDs of images to merge into album. Images should already be uploaded.
	 * @param title The title you want the album to have
	 * @return src.Imgur JSON response
	 * @throws Exception Connection problem
	 */
	public String createNewAlbum(String[] ids, String title) throws Exception {
		/*TODO Call this only once after all images have been generated for the session, 
		 * and use the comma delimeted ids array to make them all save to the album at once. 
		 * then email album link*/
		String idsCommaDelimited = "";

		for (int i = 0; i < ids.length; i++) {
			idsCommaDelimited += ids[i];
			if (i != ids.length - 1) {
				idsCommaDelimited += ", "; //Adds comma to every entry except last
			}
		}

		String idSend = URLEncoder.encode("ids", "UTF-8") + "="
				+ URLEncoder.encode(idsCommaDelimited, "UTF-8");

		String titleSend = "&" + URLEncoder.encode("title", "UTF-8") + "=" 
				+ URLEncoder.encode(title, "UTF-8");

		URL url;
		url = new URL("https://api.imgur.com/3/album");
		HttpURLConnection conn = connect("POST", url);
		conn.connect();

		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(idSend);
		wr.write(titleSend);
		wr.flush();

		StringBuilder stb = new StringBuilder();
		// Get the response
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			stb.append(line).append("\n");
		}

		rd.close();

		return stb.toString();
	}

	/**
	 * Parses the imgur JSON response
	 * @param response Raw JSON response from imgur
	 * @return the "data" response object
	 */
	public JSONObject parseJSON(String response) {
		JSONObject obj = new JSONObject(response);

		JSONObject o2 = obj.optJSONObject("data");
		String link = o2.getString("link");
		System.out.println(link);
		return o2;
	}

	/**
	 * Opens a client connection to imgur using the ID and url
	 * @param url Endpoint
	 * @return The connection, not connected yet
	 * @throws IOException Something goes wrong
	 */
	private HttpURLConnection connect(String method, URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod(method);
		conn.setRequestProperty("Authorization", "Client-ID " + clientID);
		conn.setRequestMethod(method);
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");

		return conn;
	}
}