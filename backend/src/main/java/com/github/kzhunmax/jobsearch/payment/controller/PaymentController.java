package com.github.kzhunmax.jobsearch.payment.controller;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.PricingPlan;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Endpoints for managing user subscriptions (Portfolio Stub)")
public class PaymentController {

    private final UserRepository userRepository;

    @PostMapping("/upgrade-premium")
    @Operation(summary = "Simulate upgrading to Premium",
            description = "This is a portfolio stub. It simulates a successful payment and upgrades the user's account.")
    public ResponseEntity<ApiResponse<String>> upgradeToPremium(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        User user = userDetails.getUser();

        user.setPricingPlan(PricingPlan.PREMIUM);
        userRepository.save(user);

        log.info("Request [{}]: User {} upgraded to PREMIUM", requestId, user.getEmail());

        return ApiResponse.success("Account successfully upgraded to PREMIUM.", requestId);
    }
}