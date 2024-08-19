package com.scm.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.scm.entities.Contact;
import com.scm.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	// pagination
	@Query("from Contact as c where c.user.id =:userId")
	// current page
	// contact per page-5
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);

	@Modifying
	@Transactional
	@Query(value = "delete from Contact c where c.cid = ?1")
	void deleteByIdCustom(Integer cId);

	public List<Contact> findByNameContainingAndUser(String name, User user);
	
	public Page<Contact> findByNameContainingAndUser(String name, User user,Pageable pageable);

	@Query("from Contact as c where c.user.id =:userId and ( upper(c.name) like upper(concat('%',:inputString,'%'))  "
			+ " or upper(c.secondName) like upper(concat('%',:inputString,'%'))  or concat(c.phone,'') like %:inputString% "
			+ " or upper(c.email) like upper(concat('%',:inputString,'%'))  or upper(c.work) like upper(concat('%',:inputString,'%')) )")
	List<Contact> findAllByInputString(@Param("userId") int userId, String inputString);

}
