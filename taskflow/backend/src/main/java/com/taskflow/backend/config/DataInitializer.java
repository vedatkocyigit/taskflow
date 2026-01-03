package com.taskflow.backend.config;

import com.taskflow.backend.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.taskflow.backend.entity.Role;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {

        roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("USER").build()
                ));

        roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ADMIN").build()
                ));
    }
}
