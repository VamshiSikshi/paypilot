package com.paypilot.backend.domain.user;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.dto.CreateUserRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserRoleRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserStatusRequest;
import com.paypilot.backend.domain.user.dto.UserResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MerchantRepository merchantRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                merchantRepository
        );
    }

    @Test
    void getUser_shouldReturnUserResponse() {
        Merchant merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        User user = new User(
                merchant,
                "owner@test.com",
                "Test Owner",
                UserRole.OWNER
        );

        when(userRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(1L, 10L);

        assertEquals("owner@test.com", response.email());
        assertEquals("Test Owner", response.displayName());
        assertEquals(UserRole.OWNER, response.role());
        assertEquals(UserStatus.INVITED, response.status());

        verify(userRepository).findByMerchantIdAndId(1L, 10L);
    }

    @Test
    void getUser_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByMerchantIdAndId(1L, 999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUser(1L, 999L)
        );

        verify(userRepository).findByMerchantIdAndId(1L, 999L);
    }

    @Test
    void createUser_shouldCreateUserForMerchant() {
        Merchant merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        CreateUserRequest request = new CreateUserRequest(
                "finance@test.com",
                "Test Finance",
                UserRole.FINANCE
        );

        User savedUser = new User(
                merchant,
                request.email(),
                request.displayName(),
                request.role()
        );

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = userService.createUser(1L, request);

        assertEquals("finance@test.com", response.email());
        assertEquals("Test Finance", response.displayName());
        assertEquals(UserRole.FINANCE, response.role());
        assertEquals(UserStatus.INVITED, response.status());

        verify(merchantRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowWhenMerchantDoesNotExist() {
        CreateUserRequest request = new CreateUserRequest(
                "finance@test.com",
                "Test Finance",
                UserRole.FINANCE
        );

        when(merchantRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(999L, request)
        );

        verify(merchantRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateDetails() {
        Merchant merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        User user = new User(
                merchant,
                "old@test.com",
                "Old Name",
                UserRole.VIEWER
        );

        when(userRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.of(user));

        UpdateUserRequest request = new UpdateUserRequest(
                "new@test.com",
                "New Name"
        );

        UserResponse response = userService.updateUser(
                1L,
                10L,
                request
        );

        assertEquals("new@test.com", response.email());
        assertEquals("New Name", response.displayName());

        verify(userRepository).findByMerchantIdAndId(1L, 10L);
    }

    @Test
    void updateRole_shouldUpdateUserRole() {
        Merchant merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        User user = new User(
                merchant,
                "viewer@test.com",
                "Test Viewer",
                UserRole.VIEWER
        );

        when(userRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.updateRole(
                1L,
                10L,
                new UpdateUserRoleRequest(UserRole.FINANCE)
        );

        assertEquals(UserRole.FINANCE, response.role());
        assertEquals(UserRole.FINANCE, user.getRole());

        verify(userRepository).findByMerchantIdAndId(1L, 10L);
    }

    @Test
    void updateStatus_shouldUpdateUserStatus() {
        Merchant merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        User user = new User(
                merchant,
                "viewer@test.com",
                "Test Viewer",
                UserRole.VIEWER
        );

        when(userRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.updateStatus(
                1L,
                10L,
                new UpdateUserStatusRequest(UserStatus.ACTIVE)
        );

        assertEquals(UserStatus.ACTIVE, response.status());
        assertEquals(UserStatus.ACTIVE, user.getStatus());

        verify(userRepository).findByMerchantIdAndId(1L, 10L);
    }

    @Test
    void updateStatus_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByMerchantIdAndId(1L, 999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateStatus(
                        1L,
                        999L,
                        new UpdateUserStatusRequest(UserStatus.ACTIVE)
                )
        );

        verify(userRepository).findByMerchantIdAndId(1L, 999L);
    }
}