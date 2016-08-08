package Leon;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.text.*;

public class Mail
{	
	
	
    public static void main(String[] args) throws MessagingException
    {
        Properties props = new Properties();
        InputStream input = null;
        String mailServer = "";
    	String from = "";
    	String subject = "";
    	String mailbody = "";
    	String table1 = "";
    	String table2 = "";
    	String table3 = "";
    	String addresses[] = new String[20];
    	Date d = new Date();
    	
    	Console c = System.console(); //testing purposes
    	
    	SimpleDateFormat day = new SimpleDateFormat("d"); //Day in number format
    	SimpleDateFormat day01 = new SimpleDateFormat("EEE"); //Day in letter format
		SimpleDateFormat month = new SimpleDateFormat("MMM"); //Month in number format
		SimpleDateFormat year = new SimpleDateFormat("yyyy"); //Year in number format
		
		String dia = day.format(d);
		String dialetterformat = day01.format(d);
		String mes = month.format(d);
		String ano = year.format(d);
        
        try{
        	
        	//Setting default values from input file
        	input = new FileInputStream("input.txt");
        	props.load(input);
        	mailServer = props.getProperty("mailServer");
			from = props.getProperty("from");
			subject = props.getProperty("subject");
			table1 = props.getProperty("table1");
			table2 = props.getProperty("table2");
			table3 = props.getProperty("table3");
			
			
			//Connection string into database
			String connectionUrl = "jdbc:sqlserver://xxxxxxx\\EESISQL;" +  
					   "databaseName=xxxxxxxxxxxx;user=xxxxx;password=xxxxxxxxx;";  

			//Building the body
			mailbody += "<head>";
            mailbody += "<style>";
            mailbody += "th {background-color: #E3290F; font-size: 1em; color: white;font-family:sans-serif;} ";
            mailbody += "table {background: white;width:700px}";
            mailbody += "table, td, th {border: 2px solid black;border-collapse:collapse;border-width:thin;padding: 1px 5px 1px 5px;font-size:1em;}";
            mailbody += "th { text-align: left;}";
            mailbody += "</style></head><body>";
            mailbody += "<div style='width:500px;color:black;font-family:Arial;text-align:center;'> ";
            mailbody += "<div style='text-align:center;text-decoration:underline;'><h3><em>Daily Clearing Report</em></h3></div>";
            mailbody += "<h3 style='font-size:0.90em;font-family:Arial;text-align:center;'>Trader Notifications</h3>";
            mailbody += "<table>";
            mailbody += "<tr>";
            mailbody += "<th style='width:100px'><em>Client</em></th>";
            mailbody += "<th><em>UserID</em></th>";
            mailbody += "</tr>";
								
			Properties properties = System.getProperties();
			
			properties.setProperty("mail.smtp.host", mailServer);
			properties.setProperty("mail.mime.address.strict", "false");
			
        	Connection conn = DriverManager.getConnection(connectionUrl);
        	
        	Session session = Session.getDefaultInstance(properties);
        	//Optional port use
        	//props.put("mail.smtp.port", 587);
        	
        	//In case authorization is needed
        	// props.setProperty("mail.smtp.auth", "true");
        	
        	//In case server requires password and ID
        	//Transport transport = session.getTransport("smtp");
        	//transport.connect("user", "password");

        	Statement statement = conn.createStatement();
	        
        	//Querying the first table
	        String queryString = "SELECT DISTINCT userId, Client, Description FROM " + table1 +
	        					" WHERE userId in (SELECT DISTINCT userID FROM " + table2 +
	        		 			" WHERE tradeDate like '%" + mes + "%" + dia + "%" + ano + "%') " +
	        		 			" AND Notification LIKE '%Trader%'" +
	        		 			" OR userId in (SELECT DISTINCT userID FROM " + table2 +
	        		 			" WHERE tradeDate like '%" + mes + "%" + dia + "%" + ano + "%') " +
	        		 			" AND Notification LIKE '%Both%'";
	        		 			
	        ResultSet rs = statement.executeQuery(queryString);
	        
	        while (rs.next()) {
	           mailbody += "<tr><td>" + rs.getString("Client").trim() + "</td><td>" + rs.getString("userId").toString().replace(" ", "").trim() + "</td></tr>";
	        }
	        
	        
	        //Title and beginning of second table
	        mailbody += "</table><br><br><div style='text-align:center'><h3 style='font-size:0.9em;font-family:Arial'>Co-op EOD Processing</h3></div>" +
                    "<table>" +
                    "<tr>" +
                    "<th style='width:100px'><em>Client</em></th>" +
                    "<th style='width:100px'><em>UserID</em></th>" +
                    "<th ><em>Description</em></th>" +
                    "</tr>";
	        
                    
	       Statement statement01 = conn.createStatement();
	       
	       //Querying the second table
	        String queryString01 = "SELECT DISTINCT userId, Client, Description FROM " + table1 +
	        					" WHERE userId in (SELECT DISTINCT userID FROM " + table2 +
	        		 			" WHERE tradeDate like '%" + mes + "%" + dia + "%" + ano + "%') " +
	        		 			" AND Notification LIKE '%Co-op%'" +
	        		 			" OR userId in (SELECT DISTINCT userID FROM " + table2 +
	        		 			" WHERE tradeDate like '%" + mes + "%" + dia + "%" + ano + "%') " +
	        		 			" AND Notification LIKE '%Both%'";
	        		 			
	        ResultSet rs01 = statement01.executeQuery(queryString01);
	        
	        while (rs01.next()) {
	           mailbody += "<tr><td>" + rs01.getString("Client").trim() + "</td><td>"  + rs01.getString("userId").toString().replace(" ", "").trim()  + "</td><td>" + rs01.getString("Description").toString().trim().replaceAll("[^a-zA-Z0-9\\s]", "") + "</td></tr>";
	        }
        	
        	
	        mailbody += "</table>";
	        
	        //Only display this on a Friday
	        if(dialetterformat.matches("Fri")){
	        	mailbody += "<p style='font-size: 0.7em'>*Recall whether the TIPVest Pairs Account was used this week.</p>";
	        }
        	
	        mailbody += "</div></body>";
	        
	        //Extracting email
	        Statement statement02 = conn.createStatement();
	        
	        String queryString02 = "SELECT email from " + table3;
	        
	        ResultSet rs02 = statement02.executeQuery(queryString02);
	        
	        //Displaying email addresses
	        int i = 0;
	        while(rs02.next()){
	        	addresses[i] = rs02.getString(1).toString().trim();
	        	System.out.println(addresses[i]);
	        	i++;
	        }
	        
	        //Testing print out
        	System.out.println(mailbody);
	        
	        //Quoting the ESS EOD email due to the space and single quotes
	        
	        
        	Message message = new MimeMessage(session);
        	message.setSubject(subject);
        	message.setContent(mailbody, "text/html");
        	message.setFrom(new InternetAddress(from));
        	
        	for(int x = 0; x < i; x++){
        		message.addRecipient(Message.RecipientType.TO, new InternetAddress(addresses[x]));
        	}
        	
        	//Single Email Recipient
        	//message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        	
        	//Transport.send(message); //Sends Message
        
        	//Completion message
        	System.out.println("Message Sent Successfully");
        	
        	
        }catch(Exception ex){
        	ex.printStackTrace();
        }finally {
        	if (input != null){
        		try{
        			input.close();
        		}catch (Exception e){
        			e.printStackTrace();
        		}
        	}
        	
        }
    }
    
}






