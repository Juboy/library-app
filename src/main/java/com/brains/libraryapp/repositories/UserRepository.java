package com.brains.libraryapp.repositories;

import org.springframework.data.repository.CrudRepository;

import com.brains.libraryapp.models.User;

public interface UserRepository extends CrudRepository<User, Long> {
	User findByUsername(String username);
	User findOneById(Long id);
	boolean existsByUsername(String username);
}
