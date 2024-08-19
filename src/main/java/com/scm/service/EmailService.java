package com.scm.service;

import java.util.Properties;



import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	
	
	public boolean sendEmail(String subject,String message,String to)
	{
		boolean f = false;
		
		String from = "kitkat.ankit555@gmail.com";
		//variable for gmail
		String host = "smtp.gmail.com";
		
		//get system properties
		Properties properties = System.getProperties();
		System.out.println("Properties -->"+properties);
		
		//setting important information to properties object
		
		//host set
		properties.put("mail.smtp.host",host);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//Step 1: to get the session object
		
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("kitkat.ankit555@gmail.com","clwbbsnwktkssxsb");
			}
			
			
			
		});
		
		session.setDebug(true);
		//Step 2: compose the message
		
		MimeMessage m = new MimeMessage(session);
		
		
		
		try
		{
			//from email
		  m.setFrom(from);
		  
		  //adding recipient
		  m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		  
		  //adding subject to message
		  m.setSubject(subject);
		  
		  //adding text to message
		  m.setText(message);
		  
		  //Step 3: send message to transport class
		  Transport.send(m);
		  
		  System.out.println("Sent success.........");
		  
		  f = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return f;
	}
	
	
	
    
}
