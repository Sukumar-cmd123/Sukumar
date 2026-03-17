package com.bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GenerateOtpRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Pattern(regexp = "^\\+[0-9]{12}$", message = "Mobile number must start with + and contain exactly 12 digits")
    private String mobileNumber;
}