package com.kashmira.paymentservices.controllers;

import com.kashmira.paymentservices.services.http.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<String> payment(HttpServletRequest request) {
        return  paymentService.handlePayment(request);
    }


}
