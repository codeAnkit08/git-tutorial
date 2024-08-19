package com.scm.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.scm.dao.ContactRepository;
import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.service.ContactService;

import jakarta.transaction.Transactional;

@Service
public class ContactServiceImpl implements ContactService {

	@Autowired
	ContactRepository contactRepository;

	@Override
	public Optional<Contact> findById(int cid) {
		// TODO Auto-generated method stub
		return contactRepository.findById(cid);
	}

	@Override
	public Page<Contact> findContactsByUser(int userId, Pageable pageable) {
		// TODO Auto-generated method stub
		return contactRepository.findContactsByUser(userId, pageable);
	}

	@Override
	public void deleteContact(int cid) {
		// TODO Auto-generated method stub
		contactRepository.deleteByIdCustom(cid);
	}

	@Override
	public Contact updateContact(Contact contact) {
		// TODO Auto-generated method stub
		return contactRepository.save(contact);
	}

	@Override
	public List<Contact> findByNameContainingAndUser(String keywords, User user) {
		// TODO Auto-generated method stub
		return contactRepository.findByNameContainingAndUser(keywords, user);
	}

	@Override
	public Page<Contact> findByNameContainingAndUser(String keywords, User user, Pageable pageable) {
		// TODO Auto-generated method stub
		return contactRepository.findByNameContainingAndUser(keywords, user, pageable);
	}

	@Override
	public List<Contact> findAllByInputString(int userId, String inputString) {
		// TODO Auto-generated method stub
		return contactRepository.findAllByInputString(userId, inputString);
	}

}
