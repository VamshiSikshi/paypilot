package com.paypilot.backend.domain.merchant;

import com.paypilot.backend.domain.merchant.dto.MerchantResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public MerchantResponse getMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found: " + merchantId
                ));

        return MerchantResponse.from(merchant);
    }

    @Override
    @Transactional
    public MerchantResponse updateStatus(
            Long merchantId,
            MerchantStatus status
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found: " + merchantId
                ));

        merchant.updateStatus(status);

        return MerchantResponse.from(merchant);
    }
}