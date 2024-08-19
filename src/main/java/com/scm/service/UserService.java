package com.scm.service;

import com.scm.entities.User;

public interface UserService {

	public User saveUser(User user);

	public User getUserByUserName(String UserName);

}
