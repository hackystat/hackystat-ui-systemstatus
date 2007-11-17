package org.hackystat.systemstatus.pingmail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.utilities.stacktrace.StackTrace;

/**
 * Demonstration class to show a simple form of service monitoring. 
 * Provides a main class that is invoked with a mail server, a SensorBase URL, and an email
 * address. The hackystat server is pinged and an email is generated indicating whether the ping was
 * successful. If so, the server is up and accepting requests.  
 * 
 * @author Philip Johnson
 */
public class PingMail {

  /**
   * When invoked, this main class pings the passed server and sends an email indicating whether the
   * ping was successful or not to the passed email address via the passed mail server.
   * 
   * @param args Takes three string arguments, a hackystat server, a mail server, and an email.
   */
  public static void main(String[] args) {
    
    // Make sure we have correct number of args. 
    if (!(args.length == 3)) {
      System.out.println("java -jar pingmail.jar <sensorbase> <mail server> <email>");
      return;
    }
    // Assign the args to variables for readability. 
    String sensorbase = args[0];
    String mailServer = args[1];
    String email = args[2];
    // Echo what we're going to do. 
    System.out.println("Contacting sensorbase server at: " + sensorbase);
    System.out.println("Emailing results to " + email);
    System.out.println("Using mail server " + mailServer);
    // For demo purposes, use the pre-defined test user and password. 
    String user = "TestUser@hackystat.org";
    String password = "TestUser@hackystat.org";
    
    // Start doing some checks. 
    boolean isHost = SensorBaseClient.isHost(sensorbase);
    boolean isRegistered = SensorBaseClient.isRegistered(sensorbase, user, password);
    SensorBaseClient client = new SensorBaseClient(sensorbase, user, password);
    SensorDataIndex index;
    boolean hasData;
    try {
      index = client.getSensorDataIndex(user);
      hasData = (index.getSensorDataRef().size() > 0);
    }
    catch (Exception e) {
      hasData = false;
    }
    // Create report on results. 
    String hostReport = "Attempt to contact " + sensorbase + (isHost ? " succeeds" : " fails");
    String userReport = "Attempt to login " + user + (isRegistered ? " succeeds" : " fails");
    String dataReport = "Attempt to get data " + (hasData ? "succeeds" : "fails");
    String report = hostReport + "\n" + userReport + "\n" + dataReport;
    System.out.println(report);
    sendMail(mailServer, email, "Hackystat PingMail (" + (hasData ? "OK" : "FAIL") + ")" , report);
  }

  /**
   * Sends email to the userEmail.
   * @param mailServer The mail server. 
   * @param userEmail The userEmail.
   * @param subject The subject of the email.
   * @param body The body of the email.
   */
  private static void sendMail(String mailServer, String userEmail, String subject, String body) {
    Properties props = new Properties();
    props.put("mail.smtp.host", mailServer);
    Session session = Session.getInstance(props);
    try {
      Message msg = new MimeMessage(session);
      InternetAddress userAddress = new InternetAddress(userEmail);
      InternetAddress[] adminAddressArray = { userAddress };
      msg.setFrom(userAddress);
      msg.setReplyTo(adminAddressArray);
      msg.setRecipient(Message.RecipientType.TO, userAddress);
      msg.setSubject(subject);
      msg.setSentDate(new Date());
      msg.setText(body);
      Transport.send(msg);
      System.out.println("Email regarding system status sent to " + userEmail);
    }
    catch (Exception e) {
      String msg = "Error sending mail. Perhaps JavaMail is not installed correctly? " +
      "See http://code.google.com/p/hackystat/wiki/InstallingJavaMail for instructions." +
      StackTrace.toString(e);
      System.err.println(msg);
    }
  }
}
