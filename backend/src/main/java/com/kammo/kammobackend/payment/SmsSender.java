package com.kammo.kammobackend.payment;

public interface SmsSender {

    void send(String phoneNumber, String message);
}
