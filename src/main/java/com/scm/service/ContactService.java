package com.scm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.scm.entities.Contact;
import com.scm.entities.User;

public interface ContactService {

	public Optional<Contact> findById(int cid);

	public Page<Contact> findContactsByUser(int userId, Pageable pageable);

	public void deleteContact(int cid);

	public Contact updateContact(Contact contact);
	
	public List<Contact> findByNameContainingAndUser(String keywords, User user);
	
	public Page<Contact> findByNameContainingAndUser(String keywords, User user, Pageable pageable);

	List<Contact> findAllByInputString( int userId, String inputString);
}
