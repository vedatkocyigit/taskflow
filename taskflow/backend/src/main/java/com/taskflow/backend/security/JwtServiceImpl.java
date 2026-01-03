package com.taskflow.backend.security;

import com.taskflow.backend.entity.User;

public interface JwtServiceImpl {
    String generateToken(User user);
}
