package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.Order;
import com.secondhand.electronics.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    
    List<Order> findByBuyerOrSeller(User buyer, User seller);
    
    Optional<Order> findByOrderNumber(String orderNumber);
}
