package com.aidana.wallet_api.DTO.request;

import lombok.Getter;

@Getter
public class LoginUserRequest {
    private String email;

    private String password;
}
