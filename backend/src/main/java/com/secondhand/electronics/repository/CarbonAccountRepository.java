package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.CarbonAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarbonAccountRepository extends JpaRepository<CarbonAccount, Long> {
    Optional<CarbonAccount> findByUserId(Long userId);
}
