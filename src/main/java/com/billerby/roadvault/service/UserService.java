package com.billerby.roadvault.service;

import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.User;
import com.billerby.roadvault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing users.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all users.
     *
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get a user by ID.
     *
     * @param id The user ID
     * @return The user
     * @throws ResourceNotFoundException if user is not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Find a user by username.
     *
     * @param username The username
     * @return The user
     * @throws ResourceNotFoundException if user is not found
     */
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Find a user by email.
     *
     * @param email The email
     * @return The user
     * @throws ResourceNotFoundException if user is not found
     */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Create a new user.
     *
     * @param user The user to create
     * @return The created user
     */
    @Transactional
    public User createUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role if none provided
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.addRole("ROLE_USER");
        }
        
        return userRepository.save(user);
    }

    /**
     * Update a user.
     *
     * @param id The user ID
     * @param userDetails The updated user details
     * @return The updated user
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Update basic information
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail()); // Consider additional validation for email changes
        
        // Don't update password here - use changePassword method instead
        
        return userRepository.save(user);
    }

    /**
     * Change a user's password.
     *
     * @param id The user ID
     * @param newPassword The new password
     * @return The updated user
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional
    public User changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    
    /**
     * Change a user's password with validation of current password.
     *
     * @param username The username
     * @param currentPassword The current password
     * @param newPassword The new password
     * @param confirmPassword The confirmed new password
     * @return The updated user
     * @throws ResourceNotFoundException if user is not found
     * @throws IllegalArgumentException if current password is incorrect or new passwords don't match
     */
    @Transactional
    public User changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        // Check if current password is correct
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Check if new password and confirm password match
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password don't match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    /**
     * Enable or disable a user.
     *
     * @param id The user ID
     * @param enabled The enabled status
     * @return The updated user
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional
    public User setUserEnabled(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    /**
     * Add a role to a user.
     *
     * @param id The user ID
     * @param role The role to add
     * @return The updated user
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional
    public User addUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.addRole(role);
        return userRepository.save(user);
    }

    /**
     * Remove a role from a user.
     *
     * @param id The user ID
     * @param role The role to remove
     * @return The updated user
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional
    public User removeUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.removeRole(role);
        return userRepository.save(user);
    }

    /**
     * Delete a user.
     *
     * @param id The user ID
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        userRepository.delete(user);
    }

    /**
     * Update last login time for a user.
     *
     * @param username The username
     */
    @Transactional
    public void updateLastLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
