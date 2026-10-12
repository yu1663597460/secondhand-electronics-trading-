package com.secondhand.electronics.controller;

import com.secondhand.electronics.dto.AddressDTO;
import com.secondhand.electronics.entity.Address;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.service.AddressService;
import com.secondhand.electronics.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<Address> addAddress(
            Authentication authentication,
            @Valid @RequestBody AddressDTO dto) {
        User user = userService.getUserByUsername(authentication.getName());
        Address address = addressService.addAddress(user, dto);
        return ResponseEntity.ok(address);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody AddressDTO dto) {
        User user = userService.getUserByUsername(authentication.getName());
        Address address = addressService.updateAddress(id, dto, user);
        return ResponseEntity.ok(address);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        addressService.deleteAddress(id, user);
        return ResponseEntity.ok("地址删除成功");
    }
    
    @GetMapping
    public ResponseEntity<List<Address>> getAddresses(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Address> addresses = addressService.getAddressesByUser(user);
        return ResponseEntity.ok(addresses);
    }
    
    @PutMapping("/{id}/default")
    public ResponseEntity<String> setDefaultAddress(
            @PathVariable Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        addressService.setDefaultAddress(id, user);
        return ResponseEntity.ok("默认地址设置成功");
    }
}
