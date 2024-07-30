package com.kashmira.paymentservices.services.grpc;

import com.google.protobuf.Timestamp;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import my_package.PaymentServiceGrpc;
import my_package.Payments;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
@RequiredArgsConstructor
public class PaymentServiceImpl extends PaymentServiceGrpc.PaymentServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final StripeClient stripeClient;

    @Override
    public void initiatePayment(Payments.PaymentRequestPayload request, StreamObserver<Payments.PaymentResponsePayload> responseObserver) {
        logger.info("Received payment initiation request for userID: {}", request.getUserID());

        String STRIPE_PAYMENT_METHOD = "pm_card_us";
        PaymentIntentCreateParams paymentIntentCreateParams = PaymentIntentCreateParams.builder()
                .setAmount((long) (request.getAmount() * 100)) // Convert to smallest currency unit (cents for USD)
                .setCurrency("usd")
                .setPaymentMethod(STRIPE_PAYMENT_METHOD)
                .setDescription("Testing Stripe API")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                .build();

        PaymentIntent paymentIntent;
        try {
            paymentIntent = PaymentIntent.create(paymentIntentCreateParams);
            paymentIntent.confirm();
            logger.info("PaymentIntent created successfully with ID: {}", paymentIntent.getId());
            logger.info("Client Secret: {}", paymentIntent.getClientSecret());
        } catch (StripeException e) {
            logger.error("StripeException occurred: ", e);
            responseObserver.onError(e);
            return;
        }

        // Building the response
        Payments.PaymentResponsePayload.Builder responseBuilder = Payments.PaymentResponsePayload.newBuilder()
                .setAmount(request.getAmount())
                .setUserID(request.getUserID())
                .setPropertyID(request.getPropertyID())
                .setInitiatedAt(request.getInitiatedAt())
                .setStatus(Payments.Status.SUCCESSFUL)
                .setClientSecret(paymentIntent.getClientSecret())
                .setDescription("Testing Stripe API")
                .setCreated(Timestamp.newBuilder().setSeconds(paymentIntent.getCreated()).build());


        logger.info("Response Builder State: {}", responseBuilder);

        // Verify all fields are set correctly
        Payments.PaymentResponsePayload response = responseBuilder.build();
        logger.info("Response prepared with clientSecret: {}", response.getClientSecret());
        logger.info("Response: {}", response);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
