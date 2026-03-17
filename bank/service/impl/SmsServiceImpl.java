package com.bank.service.impl;

import com.bank.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${app.sms.simulate:false}")
    private boolean simulateSms;

    @Value("${twilio.accountSid}")
    private String twilioAccountSid;

    @Value("${twilio.authToken}")
    private String twilioAuthToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    @Override
    public void sendOtp(String phone, String otp) {
        if (simulateSms) {
            log.info("[SIMULATION] OTP SMS to {}: {}", phone, otp);
            return;
        }
        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message message = Message.creator(
                    new PhoneNumber(phone),
                    new PhoneNumber(twilioPhoneNumber),
                    "Your OTP for Banking App is: " + otp
            ).create();
            log.info("Twilio SMS sent to {}: SID {}", phone, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {} via Twilio. Error: {}", phone, e.getMessage());
        }
    }
}