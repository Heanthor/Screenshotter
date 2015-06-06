import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

/**
 * Mail class to send messages use JavaMail.
 * Uses xxthrowawayxx@yahoo.com to send messages. Not very anonymous.
 * @author Reed
 */
public class Mailer {
	/**
	 * The email address used to send messages.
	 */
	private final String EMAIL_ADDRESS = "xxthrowawayxx@yahoo.com";
	
	private class YahooAuthenticator extends Authenticator {
		String user;
		String pw;

		public YahooAuthenticator(String user, String pw) {
			this.user = user;
			this.pw = pw;
		}

		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, pw);
		}
	}
	private String to, subject;

	/**
	 * @param to Destination address
	 * @param subject Message subject
	 */
	public Mailer(String to, String subject) {
		this.to = to;

		this.subject = subject;
	}

	/**
	 * Sends message using current parameters.
	 * @param m - Message to send
	 * @param debug - Whether or not to output JavaMail debug messages
	 * @throws FileNotFoundException - The login info file cannot be located
	 */
	public void sendMail(String m, boolean debug) throws FileNotFoundException {
		//Form: username \n password
		BufferedReader br = new BufferedReader(new FileReader("login.txt"));
		
		String user = null;
		String password = null;
		
		try {
			user = br.readLine().trim();
			password = br.readLine().trim();
			
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Properties p = new Properties();

		//SMTP properties
		p.setProperty("mail.smtp.starttls.enable", "true");
		p.setProperty("mail.smtp.host", "smtp.mail.yahoo.com");
		p.setProperty("mail.smtp.user", user);
		p.setProperty("mail.smtp.password", password);
		p.setProperty("mail.smtp.port", "587");
		p.setProperty("mail.smtp.auth", "true");

		if (debug) {
			p.setProperty("mail.debug", "true");
		}
		Session session = Session.getDefaultInstance(p, new YahooAuthenticator(user, password));

		//Sending the message
		try {
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(EMAIL_ADDRESS));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			message.setSubject(subject);

			message.setText(m);

			Transport.send(message);
			System.out.println("Sent message successfully.");
		} catch (MessagingException e) {
			System.err.println("Error sending message: ");
			e.printStackTrace();
		}
	}
}
