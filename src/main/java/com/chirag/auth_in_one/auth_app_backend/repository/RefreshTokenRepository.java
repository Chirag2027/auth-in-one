package com.chirag.auth_in_one.auth_app_backend.repository;

import com.chirag.auth_in_one.auth_app_backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
}
