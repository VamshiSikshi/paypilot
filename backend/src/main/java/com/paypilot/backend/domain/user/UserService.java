package com.paypilot.backend.domain.user;

import com.paypilot.backend.domain.user.dto.CreateUserRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserRoleRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserStatusRequest;
import com.paypilot.backend.domain.user.dto.UserResponse;

public interface UserService {

    UserResponse getUser(Long merchantId, Long userId);

    UserResponse createUser(
            Long merchantId,
            CreateUserRequest request
    );

    UserResponse updateUser(
            Long merchantId,
            Long userId,
            UpdateUserRequest request
    );

    UserResponse updateRole(
            Long merchantId,
            Long userId,
            UpdateUserRoleRequest request
    );

    UserResponse updateStatus(
            Long merchantId,
            Long userId,
            Long actorUserId,
            UpdateUserStatusRequest request
    );
}