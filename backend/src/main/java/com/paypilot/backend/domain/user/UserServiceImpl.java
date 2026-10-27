package com.paypilot.backend.domain.user;

import com.paypilot.backend.domain.audit.AuditLogService;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.dto.CreateUserRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserRoleRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserStatusRequest;
import com.paypilot.backend.domain.user.dto.UserResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final AuditLogService auditLogService;

    public UserServiceImpl(
            UserRepository userRepository,
            MerchantRepository merchantRepository,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public UserResponse getUser(Long merchantId, Long userId) {
        User user = findUser(merchantId, userId);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(
            Long merchantId,
            CreateUserRequest request
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found: " + merchantId
                ));

        User user = new User(
                merchant,
                request.email(),
                request.displayName(),
                request.role()
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(
            Long merchantId,
            Long userId,
            UpdateUserRequest request
    ) {
        User user = findUser(merchantId, userId);

        user.updateDetails(
                request.email(),
                request.displayName()
        );

        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(
            Long merchantId,
            Long userId,
            UpdateUserRoleRequest request
    ) {
        User user = findUser(merchantId, userId);

        user.updateRole(request.role());

        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(
            Long merchantId,
            Long userId,
            Long actorUserId,
            UpdateUserStatusRequest request
    ) {
        User user = findUser(merchantId, userId);

        UserStatus previousStatus = user.getStatus();
        UserStatus newStatus = request.status();

        if (previousStatus != newStatus) {
            user.updateStatus(newStatus);

            String metadata = """
                    {
                      "previousStatus": "%s",
                      "newStatus": "%s"
                    }
                    """.formatted(previousStatus, newStatus);

            auditLogService.createAuditLog(
                    merchantId,
                    actorUserId,
                    "USER_STATUS_CHANGED",
                    "USER",
                    userId,
                    "User status changed from "
                            + previousStatus + " to " + newStatus,
                    metadata,
                    null,
                    null
            );
        }

        return UserResponse.from(user);
    }

    private User findUser(Long merchantId, Long userId) {
        return userRepository.findByMerchantIdAndId(merchantId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userId
                ));
    }
}