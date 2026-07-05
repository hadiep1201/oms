package com.example.aims.payment;

public interface IPaymentConfirmation {

    PaymentConfirmationResult confirm(PaymentConfirmationCommand command);
}