package com.aidana.wallet_api.DTO.request;

import lombok.Getter;

@Getter
public class RegisterUserRequest {
    private String firstName;

    private String lastName;

    private String email;

    private String password;
}
