package com.scm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scm.dao.UserRepository;
import com.scm.entities.User;
import com.scm.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Override
	public User saveUser(User user) {
		// TODO Auto-generated method stub
		return userRepository.save(user);
	}

	@Override
	public User getUserByUserName(String UserName) {
		// TODO Auto-generated method stub
		return userRepository.getUserByUserName(UserName);
	}

}
