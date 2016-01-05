package src;

import java.util.ArrayList;

import org.json.JSONObject;

/**
 * Testing functions.
 * @author Reed
 *
 */
public class Test {

	public static void main(String[] args) {
		final String CLIENT_ID = "ec6057527973e93";
		
		try {
			//System.setOut(new PrintStream("test_debug.txt"));
			Imgur i = new Imgur(CLIENT_ID);
			ArrayList<String> t = new ArrayList<String>();
			t.add("JnItW");
			t.add("KW2a6HN");
			t.add("EQVQI");
			
			String[] tA = t.toArray(new String[t.size()]);
			//String[] pics = {"JnItW", "KW2a6HN", "EQVQI"};
			String response = i.createNewAlbum(tA, "TestTitle");
			JSONObject o = new JSONObject(response);
			JSONObject o2 = o.optJSONObject("data");
			
			String id = o2.getString("id");
			//System.out.println(i.addImageToAlbum("KW2a6HN", id));
			System.out.println("imgur.com/a/" + id);
			
			Mailer m = new Mailer("reedtrevelyan@gmail.com", "test");
			m.sendMail("This is a test message", true);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}
