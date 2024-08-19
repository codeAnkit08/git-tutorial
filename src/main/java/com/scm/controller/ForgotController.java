package com.scm.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.entities.User;
import com.scm.helper.Message;
import com.scm.service.EmailService;
import com.scm.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserService userService;
	@Autowired
	private BCryptPasswordEncoder bCrypt;
	
	Random random = new Random(1000);
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,Model m,HttpSession session)
	{
		System.out.println("Email-->"+email);
		
		//generating random otp of 4 digits
		
		
		int otp = random.nextInt(999999);
		
		String subject = "OTP from Smart Contact Manager";
		
		String message =""
				
				
				+ " OTP is "  
				+otp;
		
		boolean flag = this.emailService.sendEmail(subject, message, email);
		
		if(flag)
		{
			session.setAttribute("otp", otp);
			session.setAttribute("email", email);
			
			//m.addAttribute("message", new Message("You have entered wrong otp", "alert-danger"));
			return "verify_otp";
		}
		else
		{	
			m.addAttribute("message", new Message("Check your email id !!", "alert-danger"));
			
			return "forgot_email_form";
		}
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp")int otp,HttpSession session, Model m)
	{
		int myOtp = (int)session.getAttribute("otp");
		
		String email = (String)session.getAttribute("email");
		
		System.out.println("OTP is -->"+otp);
		System.out.println("My Otp is-->"+myOtp);
		System.out.println("email is-->"+email);
		System.out.println(myOtp==otp);
		if(myOtp==otp)
		{
			User user = this.userService.getUserByUserName(email);
			if(user==null)
			{
				m.addAttribute("message", new Message("User does not exist with this email !!", "alert-danger"));
				
				return "forgot_email_form";
				
			}
			else
			{
				return "password_change_form";
				
			}
			
		}
		else
		{
			m.addAttribute("message", new Message("You have entered wrong otp", "alert-danger"));
			return "verify_otp";
		}
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session)
	{
		String email = (String)session.getAttribute("email");
		User user = this.userService.getUserByUserName(email);
		user.setPassword(this.bCrypt.encode(newpassword));
		this.userService.saveUser(user);
		return "redirect:/signin?change=password changed successfully..";
	}

}
