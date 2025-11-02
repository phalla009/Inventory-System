package com.krsm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.krsm.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

	Optional<Users> findByUsernameAndPasswordAndRole(String username, String password, String role);

	@Query("SELECT DISTINCT u.role FROM Users u")
	List<String> findDistinctRoles();
}
