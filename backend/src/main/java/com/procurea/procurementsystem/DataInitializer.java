package com.procurea.procurementsystem;

import com.procurea.procurementsystem.model.Role;
import com.procurea.procurementsystem.model.User;
import com.procurea.procurementsystem.repository.RoleRepository;
import com.procurea.procurementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName(Role.ERole.ROLE_ADMIN).isEmpty()) {
            for (Role.ERole eRole : Role.ERole.values()) {
                roleRepository.save(new Role(null, eRole));
            }

            User admin = new User("admin", "admin@procurea.com", passwordEncoder.encode("admin123"));
            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByName(Role.ERole.ROLE_ADMIN).get());
            admin.setRoles(roles);
            userRepository.save(admin);
        }
    }
}
