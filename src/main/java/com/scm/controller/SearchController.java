package com.scm.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.service.ContactService;
import com.scm.service.UserService;

@RestController
public class SearchController {
	@Autowired
	private UserService userService;
	@Autowired
	private ContactService contactService;
	
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query,Principal principal)
	{
		User user = userService.getUserByUserName(principal.getName());
		
		List<Contact> contacts = contactService.findAllByInputString(user.getId(), query);
		
		return ResponseEntity.ok(contacts);
		
	}

}
