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

	// Authenticate user by username and password only.
	// Role is NOT selected by the user anymore — it is read from the
	// matched user record afterwards (e.g. user.getRole()) and used
	// to control permissions/redirects.
	public Users authenticate(String username, String password) {
		return usersRepository.findByUsernameAndPassword(username, password).orElse(null);
	}
}