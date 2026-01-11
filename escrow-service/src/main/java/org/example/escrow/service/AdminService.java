package org.example.escrow.service;

import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.admin.MerchantProfileResponse;
import org.example.escrow.dto.admin.UserResponse;
import org.example.escrow.dto.mapper.AdminMapper;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(adminMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MerchantProfileResponse> getAllMerchants() {
        // The Transactional annotation keeps the session open here
        // so accessing merchant.getUser() inside the mapper works.
        return merchantProfileRepository.findAll().stream()
                .map(adminMapper::toMerchantResponse)
                .collect(Collectors.toList());
    }
}