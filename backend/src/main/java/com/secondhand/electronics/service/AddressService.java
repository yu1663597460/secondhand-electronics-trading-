package com.secondhand.electronics.service;

import com.secondhand.electronics.dto.AddressDTO;
import com.secondhand.electronics.entity.Address;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    
    private final AddressRepository addressRepository;
    
    @Transactional
    public Address addAddress(User user, AddressDTO dto) {
        Address address = new Address();
        address.setUser(user);
        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        
        // 如果是第一个地址，自动设为默认
        List<Address> existingAddresses = addressRepository.findByUser(user);
        if (existingAddresses.isEmpty()) {
            address.setIsDefault(true);
        }
        
        return addressRepository.save(address);
    }
    
    @Transactional
    public Address updateAddress(Long addressId, AddressDTO dto, User user) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException("地址不存在", 404));
        
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权限操作此地址", 403);
        }
        
        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        
        return addressRepository.save(address);
    }
    
    @Transactional
    public void deleteAddress(Long addressId, User user) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException("地址不存在", 404));
        
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权限操作此地址", 403);
        }
        
        addressRepository.delete(address);
    }
    
    public List<Address> getAddressesByUser(User user) {
        return addressRepository.findByUser(user);
    }
    
    @Transactional
    public void setDefaultAddress(Long addressId, User user) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException("地址不存在", 404));
        
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权限操作此地址", 403);
        }
        
        // 取消其他默认地址
        List<Address> addresses = addressRepository.findByUser(user);
        addresses.forEach(addr -> addr.setIsDefault(false));
        addressRepository.saveAll(addresses);
        
        // 设置新的默认地址
        address.setIsDefault(true);
        addressRepository.save(address);
    }
}
