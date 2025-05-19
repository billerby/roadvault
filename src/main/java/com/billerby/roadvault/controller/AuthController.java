package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.ChangePasswordRequest;
import com.billerby.roadvault.dto.ChangePasswordResponse;
import com.billerby.roadvault.dto.LoginRequest;
import com.billerby.roadvault.dto.LoginResponse;
import com.billerby.roadvault.dto.RegisterRequest;
import com.billerby.roadvault.dto.UserDTO;
import com.billerby.roadvault.model.User;
import com.billerby.roadvault.security.JwtUtils;
import com.billerby.roadvault.service.UserService;
import com.billerby.roadvault.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for authentication and user management.
 */
@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    
    @Autowired
    public AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        // Add this logging
        System.out.println("AuthenticationManager initialized: " + (authenticationManager != null));
    
    }
    
    /**
     * POST /v1/auth/login : Authenticate a user.
     *
     * @param loginRequest the login request
     * @return the ResponseEntity with status 200 (OK) and with body the login response
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        User user = userService.findUserByUsername(loginRequest.getUsername());
        userService.updateLastLogin(loginRequest.getUsername());
        
        List<String> roles = user.getRoles().stream()
                .map(role -> role.replaceFirst("ROLE_", ""))
                .collect(Collectors.toList());
        
        LoginResponse response = new LoginResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /v1/auth/register : Register a new user.
     *
     * @param registerRequest the registration request
     * @return the ResponseEntity with status 201 (Created) and with body the registered user
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> registerUser(@RequestBody RegisterRequest registerRequest) {
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); // Will be encoded in the service
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        
        // Set roles
        if (registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty()) {
            for (String role : registerRequest.getRoles()) {
                user.addRole("ROLE_" + role.toUpperCase());
            }
        } else {
            // Default role
            user.addRole("ROLE_USER");
        }
        
        User createdUser = userService.createUser(user);
        
        return new ResponseEntity<>(convertToDTO(createdUser), HttpStatus.CREATED);
    }
    
    /**
     * POST /v1/auth/change-password : Change the current user's password.
     *
     * @param changePasswordRequest the password change request
     * @return the ResponseEntity with status 200 (OK) and with body containing status message
     */
    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        // Get the current authenticated username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        try {
            userService.changePassword(
                    currentUsername,
                    changePasswordRequest.getCurrentPassword(),
                    changePasswordRequest.getNewPassword(),
                    changePasswordRequest.getConfirmPassword()
            );
            
            return ResponseEntity.ok(new ChangePasswordResponse("Password changed successfully", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ChangePasswordResponse(e.getMessage(), false));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ChangePasswordResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChangePasswordResponse("An error occurred while changing password", false));
        }
    }
    
    /**
     * Convert a User entity to a UserDTO.
     *
     * @param user the entity to convert
     * @return the converted DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEnabled(user.isEnabled());
        return dto;
    }
}
