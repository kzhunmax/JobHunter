package com.github.kzhunmax.jobsearch.payment.controller;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.payment.CheckoutSessionResponse;
import com.github.kzhunmax.jobsearch.payment.service.PaymentService;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Endpoints for managing user subscriptions")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook endpoint", description = "Listens for events from Stripe (e.g., payment success)")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        return paymentService.handleWebhook(payload, sigHeader, requestId);
    }

    @PostMapping("/create-checkout-session")
    @Operation(summary = "Create a Stripe Checkout session to upgrade to Premium",
            description = "Creates a Stripe session and returns a URL for the user to complete payment.")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createCheckoutSession(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws StripeException {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        CheckoutSessionResponse response = paymentService.createCheckoutSession(userDetails.getUser(), requestId);
        return ApiResponse.success(response, requestId);
    }

    @RequestMapping("/success")
    public String paymentSuccess() {
        return "Payment Successful! Your account will be upgraded shortly.";
    }

    @RequestMapping("/cancel")
    public String paymentCancel() {
        return "Payment Canceled. You have not been charged.";
    }
}