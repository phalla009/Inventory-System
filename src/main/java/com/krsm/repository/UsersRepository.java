package com.krsm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.krsm.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

	// Used for login: match by username + password only.
	// Role is looked up from the returned entity, not supplied by the user.
	Optional<Users> findByUsernameAndPassword(String username, String password);

	@Query("SELECT DISTINCT u.role FROM Users u")
	List<String> findDistinctRoles();
}