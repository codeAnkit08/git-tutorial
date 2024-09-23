package com.scm.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.helper.Message;
import com.scm.service.ContactService;
import com.scm.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserService userService;

	String className = "UserController";
	@Autowired
	private ContactService contactService;

	Logger logger = LoggerFactory.getLogger(UserController.class);

	// adding common data to users
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {

		logger.info(" SmartContactManager :: " + className + " :: addCommonData :: starts :: ");

		String userName = principal.getName();

		User user = userService.getUserByUserName(userName);

		m.addAttribute("user", user);

		logger.info(" SmartContactManager :: " + className + " :: addCommonData :: ends :: ");
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		logger.info(" SmartContactManager :: " + className + " :: dashboard :: starts :: ");

		model.addAttribute("title", "User Dashboard");

		logger.info(" SmartContactManager :: " + className + " :: dashboard :: ends :: ");

		return "normal/user_dashboard";

	}

	// open add form controller
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		logger.info(" SmartContactManager :: " + className + " :: openAddContactForm :: starts :: ");

		model.addAttribute("title", "Add Contact");

		model.addAttribute("contact", new Contact());

		logger.info(" SmartContactManager :: " + className + " :: openAddContactForm :: ends :: ");

		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@Validated @ModelAttribute("contact") Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile file, Model model, Principal principal) {
		logger.info(" SmartContactManager :: " + className + " :: processContact :: starts :: ");

		try {
			logger.info(" SmartContactManager :: " + className + " :: processContact :: inside try :: starts :: ");

			if (result.hasErrors()) {

				model.addAttribute("contact", contact);
				return "normal/add_contact_form";
			}

			String name = principal.getName();
			User user = this.userService.getUserByUserName(name);

			// processing and uploading file
			if (file.isEmpty()) {
				// if file is empty then try our message
				contact.setImage("Contact.png");
			} else {
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			contact.setUser(user);

			user.getContacts().add(contact);

			this.userService.saveUser(user);

			model.addAttribute("contact", new Contact());

			model.addAttribute("message", new Message("Contact has been saved !!", "alert-success"));

			logger.info(" SmartContactManager :: " + className + " :: processContact :: inside try :: ends :: ");

		} catch (Exception e) {

			logger.info(" SmartContactManager :: " + className + " :: processContact :: error :: " + e.getMessage());

			model.addAttribute("contact", contact);

			model.addAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));

		}

		return "normal/add_contact_form";
	}

	// show contacts handler
	// per page = 5[n]
	// current page = 0[]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		try {
			logger.info(" SmartContactManager :: " + className + " :: showContacts :: inside try :: starts :: ");

			String userName = principal.getName();

			User user = this.userService.getUserByUserName(userName);

			m.addAttribute("title", "Show User Contacts");

			Pageable pageable = PageRequest.of(page, 5);

			Page<Contact> contacts = this.contactService.findContactsByUser(user.getId(), pageable);
            
			m.addAttribute("contacts", contacts);
			m.addAttribute("currentPage", page);
			m.addAttribute("totalPages", contacts.getTotalPages());

		} catch (Exception e) {
			logger.error(" SmartContactManager :: " + className + " :: showContacts :: error :: " + e.getMessage());

		}
		return "normal/show_contacts";
	}

	// showing particular contact
	@GetMapping("/contact/{cId}")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		logger.info(" SmartContactManager :: " + className + " :: showContactDetail :: starts :: ");

		Optional<Contact> contactOptional = this.contactService.findById(cId);
		Contact contact = contactOptional.get();

		String userName = principal.getName();
		User user = this.userService.getUserByUserName(userName);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		logger.info(" SmartContactManager :: " + className + " :: showContactDetail :: ends :: ");

		return "normal/contact-detail";
	}

	// delete contact handler
	@GetMapping("/delete/{cId}/{currentPage}")
	public String deleteContact(@PathVariable("cId") Integer cId, @PathVariable("currentPage") Integer page,
			RedirectAttributes redirectAttrs, Principal principal) throws IOException {
		logger.info(" SmartContactManager :: " + className + " :: deleteContact :: starts :: ");

		try {
			Optional<Contact> contactOptional = this.contactService.findById(cId);
			Contact contact = contactOptional.get();

			String userName = principal.getName();
			User user = this.userService.getUserByUserName(userName);
			if (user.getId() == contact.getUser().getId()) {

				contact.setUser(null);
				// code to delete photo from folder
				if (contact.getImage() != null && !contact.getImage().equalsIgnoreCase("Contact.png")) {
					File deleteFile = new ClassPathResource("static/img").getFile();
					File file1 = new File(deleteFile, contact.getImage());
					file1.delete();
				}
				this.contactService.deleteContact(cId);
			}
			redirectAttrs.addFlashAttribute("message",
					new Message("Contact has been deleted successfully !!", "alert-success"));
		} catch (Exception e) {
			logger.error(" SmartContactManager :: " + className + " :: deleteContact :: error :: " + e.getMessage());
		}

		logger.info(" SmartContactManager :: " + className + " :: deleteContact :: ends :: ");

		return "redirect:/user/show-contacts/" + page;
	}

	// Get update contact details
	@GetMapping("/update-contact/{cId}")
	public String updateContact(@PathVariable("cId") Integer cId, Model model) {
		logger.info(" SmartContactManager :: " + className + " :: updateContact :: starts :: ");

		model.addAttribute("title", "Update Contact");
		Optional<Contact> contactOptional = this.contactService.findById(cId);
		Contact contact = contactOptional.get();
		model.addAttribute("contact", contact);

		logger.info(" SmartContactManager :: " + className + " :: updateContact :: ends :: ");

		return "normal/update_contact";
	}

	// process update
	@PostMapping("/process-update")
	public String updateHandler(@Validated @ModelAttribute("contact") Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile file, Model model, Principal principal,
			RedirectAttributes redirectAttrs) {
		logger.info(" SmartContactManager :: " + className + " :: updateHandler :: starts :: ");

		try {

			if (result.hasErrors()) {

				model.addAttribute("contact", contact);
				return "normal/update_contact";
			}

			Contact oldContactDetails = this.contactService.findById(contact.getCid()).get();
			String name = principal.getName();
			User user = this.userService.getUserByUserName(name);
			if (!file.isEmpty()) {
				// if file is empty then try our message
				// contact.setImage("Contact.png");

				// delete old photo

				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();

				// update new photo
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());
			} else {

				contact.setImage(oldContactDetails.getImage());
			}
			contact.setUser(user);

			this.contactService.updateContact(contact);

			redirectAttrs.addFlashAttribute("message", new Message("Contact has been updated !!", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(" SmartContactManager :: " + className + " :: updateHandler :: error :: " + e.getMessage());

			redirectAttrs.addFlashAttribute("contact", contact);

			redirectAttrs.addFlashAttribute("message",
					new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));
		}

		logger.info(" SmartContactManager :: " + className + " :: updateHandler :: ends :: ");

		return "redirect:/user/update-contact/" + contact.getCid();
	}
	
	@GetMapping("/show-contact/{cid}")
	public String showContact(@PathVariable("cid") Integer cid, Model m, Principal principal) {
		try {
			logger.info(" SmartContactManager :: " + className + " :: showContacts :: inside try :: starts :: ");

			String userName = principal.getName();

			User user = this.userService.getUserByUserName(userName);

			m.addAttribute("title", "Show User Contacts");

			Pageable pageable = PageRequest.of(0, 5);
			
			Optional<Contact> contactOptional = this.contactService.findById(cid);
			Contact contact = contactOptional.get();

			Page<Contact> contacts = this.contactService.findByNameContainingAndUser(contact.getName(), user, pageable);

			m.addAttribute("contacts", contacts);
			m.addAttribute("currentPage", 0);
			m.addAttribute("totalPages", 1);
			
		} catch (Exception e) {
			logger.error(" SmartContactManager :: " + className + " :: showContacts :: error :: " + e.getMessage());

		}
		return "normal/show_contacts";
	}

	@GetMapping("/open-settings")
	public String openSettings(Model model, Principal principal) {
		logger.info(" SmartContactManager :: " + className + " :: openSettings :: starts :: ");

		model.addAttribute("title", "Settings");

		logger.info(" SmartContactManager :: " + className + " :: openSettings :: ends :: ");

		return "normal/Settings";

	}

	@GetMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword, Principal principal,RedirectAttributes redirectAttrs) {
		logger.info(" SmartContactManager :: " + className + " :: changePassword :: starts :: ");

		logger.info(" SmartContactManager :: " + className + " :: oldPassword :: "+oldPassword);
		
		logger.info(" SmartContactManager :: " + className + " :: newPassword :: "+newPassword);
		
		String userName = principal.getName();
		
		User currentUser = this.userService.getUserByUserName(userName);
		
		logger.info(" SmartContactManager :: " + className + " :: current password :: "+currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change the password
			
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userService.saveUser(currentUser);
			
			redirectAttrs.addFlashAttribute("message",
					new Message("Password has been changed !!", "alert-success"));
		}
		else
		{
			redirectAttrs.addFlashAttribute("message",
					new Message("You entered incorrect old password !!", "alert-danger"));
			
			return "redirect:/user/open-settings";
		}
			
		logger.info(" SmartContactManager :: " + className + " :: changePassword :: ends :: ");

		return "redirect:/user/index";

	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfie(Model model)
	{
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data) throws RazorpayException
	{
		System.out.println("Hey order function executed"+data);
		
		int amt = Integer.parseInt(data.get("amount").toString());
		var client = new RazorpayClient("rzp_test_aPLWoiumf2sP6J", "g2Nja1QBJDD3ftdS4gqjsIf6");
		JSONObject options = new JSONObject();
		options.put("amount", amt*100);
		options.put("currency", "INR");
		options.put("receipt", "txn_123456");
		Order order = client.orders.create(options);

		System.out.println(order);
		return order.toString();
	}
}