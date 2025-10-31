package com.github.kzhunmax.jobsearch.payment.service;

import com.github.kzhunmax.jobsearch.payment.CheckoutSessionResponse;
import com.github.kzhunmax.jobsearch.security.PricingPlan;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
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

    public ResponseEntity<String> handleWebhook(String payload, String sigHeader, String requestId) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Request [{}]: Stripe webhook signature verification failed.", requestId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Request [{}]: Unexpected error verifying Stripe webhook signature.", requestId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error: " + e.getMessage());
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        if (dataObjectDeserializer.getObject().isEmpty()) {
            log.error("Request [{}]: Failed to deserialize Stripe event data", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook Error: Deserialization failed");
        }

        StripeObject stripeObject = dataObjectDeserializer.getObject().get();

        if (event.getType().equals("checkout.session.completed")) {
            Session session = (Session) stripeObject;
            handleCheckoutSessionCompleted(session, requestId);
        } else {
            log.warn("Request [{}]: Unhandled Stripe event type: {}", requestId, event.getType());
        }

        return new ResponseEntity<>("Event Received", HttpStatus.OK);
    }

    private void handleCheckoutSessionCompleted(Session session, String requestId) {
        String clientReferenceId = session.getClientReferenceId();
        if (StringUtils.isBlank(clientReferenceId)) {
            log.error("Request [{}]: Webhook Error: checkout.session.completed event without client_reference_id", requestId);
            return;
        }

        Long userId;
        try {
            userId = Long.parseLong(clientReferenceId);
        } catch (NumberFormatException e) {
            log.error("Request [{}]: Invalid userId in client_reference_id: {}", requestId, clientReferenceId, e);
            return;
        }

        userRepository.findById(userId).ifPresentOrElse(
                user -> {
                    user.setPricingPlan(PricingPlan.PREMIUM);
                    userRepository.save(user);
                    log.info("Request [{}]: User {} successfully upgraded to PREMIUM", requestId, user.getEmail());
                },
                () -> log.error("Request [{}]: User not found for ID {} from Stripe session {}", requestId, userId, session.getId())
        );
    }

    public CheckoutSessionResponse createCheckoutSession(User user, String requestId) throws StripeException {
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

        log.info("Request [{}]: Created Stripe Checkout session {} for user {}", requestId, session.getId(), user.getEmail());

        return new CheckoutSessionResponse(session.getUrl());
    }
}
