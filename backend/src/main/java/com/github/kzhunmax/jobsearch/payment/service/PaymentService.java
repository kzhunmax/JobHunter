package com.github.kzhunmax.jobsearch.payment.service;

import com.github.kzhunmax.jobsearch.payment.CheckoutSessionResponse;
import com.github.kzhunmax.jobsearch.security.PricingPlan;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.backend.url}")
    private String backendUrl;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.premium-price-id}")
    private String premiumPriceId;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public ResponseEntity<String> handleWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize Stripe event data"));

            if (event.getType().equals("checkout.session.completed")) {
                handleCheckoutSessionCompleted((Session) stripeObject);
            } else {
                log.warn("Unhandled Stripe event type: {}", event.getType());
            }
            return ResponseEntity.ok("Event Received");
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed.", e);
            return ResponseEntity.badRequest().body("Webhook Error: Signature verification failed");
        } catch (Exception e) {
            log.error("Unexpected error processing Stripe webhook.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook Error: " + e.getMessage());
        }
    }

    private void handleCheckoutSessionCompleted(Session session) {
        String clientReferenceId = session.getClientReferenceId();
        if (StringUtils.isBlank(clientReferenceId)) {
            log.error("Webhook Error: checkout.session.completed event without client_reference_id");
            return;
        }

        Long userId;
        try {
            userId = Long.parseLong(clientReferenceId);
        } catch (NumberFormatException e) {
            log.error("Invalid userId in client_reference_id: {}", clientReferenceId, e);
            return;
        }

        userRepository.findById(userId).ifPresentOrElse(
                user -> {
                    user.setPricingPlan(PricingPlan.PREMIUM);
                    userRepository.save(user);
                    log.info("User {} successfully upgraded to PREMIUM", user.getEmail());
                },
                () -> log.error("User not found for ID {} from Stripe session {}", userId, session.getId())
        );
    }

    public CheckoutSessionResponse createCheckoutSession(User user) throws StripeException {
        String successUrl = backendUrl + "/api/payments/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = backendUrl + "/api/payments/cancel";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(premiumPriceId)
                        .setQuantity(1L)
                        .build())
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(user.getId().toString())
                .build();

        Session session = Session.create(params);

        log.info("Created Stripe Checkout session {} for user {}", session.getId(), user.getEmail());

        return new CheckoutSessionResponse(session.getUrl());
    }
}
