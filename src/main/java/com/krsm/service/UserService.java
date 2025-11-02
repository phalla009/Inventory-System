package com.krsm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.krsm.entity.Users;
import com.krsm.repository.UsersRepository;

@Service
public class UserService {

	private final UsersRepository usersRepository;

	public UserService(UsersRepository usersRepository) {
		this.usersRepository = usersRepository;
	}

	// Save user
	public Users saveUser(Users user) {
		if (user.getCreated_at() == null) {
			user.setCreated_at(LocalDateTime.now());
		}
		return usersRepository.save(user);
	}

	// Delete user
	public void deleteUser(Long id) {
		usersRepository.deleteById(id);
	}

	// Get all users
	public List<Users> getAllUsers() {
		return usersRepository.findAll();
	}

	// Get user by ID
	public Users getUserById(Long id) {
		return usersRepository.findById(id).orElse(null);
	}

	// Get all distinct roles (for login dropdown)
	public List<String> getAllRoles() {
		return usersRepository.findDistinctRoles(); // custom query in repository
	}

	// Authenticate user by username, password, and role
	public Users authenticate(String username, String password, String role) {
		return usersRepository.findByUsernameAndPasswordAndRole(username, password, role).orElse(null);
	}
}
