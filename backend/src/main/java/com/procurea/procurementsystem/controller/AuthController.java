package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.*;
import com.procurea.procurementsystem.entity.Role;
import com.procurea.procurementsystem.entity.User;
import com.procurea.procurementsystem.repository.RoleRepository;
import com.procurea.procurementsystem.repository.UserRepository;
import com.procurea.procurementsystem.security.JwtUtils;
import com.procurea.procurementsystem.security.UserDetailsImpl;
import com.procurea.procurementsystem.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private VendorService vendorService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        JwtResponse jwtResponse = new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);

        return ResponseEntity.ok(ApiResponse.success("Sign in successful", jwtResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MessageResponse>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        boolean isVendor = false;

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(Role.ERole.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            for (String role : strRoles) {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "procurement":
                    case "manager":
                        Role modRole = roleRepository.findByName(Role.ERole.ROLE_PROCUREMENT_MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    case "vendor":
                        Role vendorRole = roleRepository.findByName(Role.ERole.ROLE_VENDOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(vendorRole);
                        isVendor = true;
                        break;
                    default:
                        Role userRole = roleRepository.findByName(Role.ERole.ROLE_EMPLOYEE)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            }
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // If user is a vendor, initialize blank vendor profile
        if (isVendor) {
            VendorDto vendorDto = new VendorDto();
            vendorDto.setCompanyName(savedUser.getUsername() + " Company");
            vendorDto.setEmail(savedUser.getEmail());
            vendorDto.setPhoneNumber("");
            vendorDto.setAddress("");
            vendorDto.setCategory("General");
            vendorService.registerVendor(vendorDto, savedUser.getId());
        }

        return ResponseEntity.ok(ApiResponse.success("User registered successfully!", new MessageResponse("User registered successfully!")));
    }
}
