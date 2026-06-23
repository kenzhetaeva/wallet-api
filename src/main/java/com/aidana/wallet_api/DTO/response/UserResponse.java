package com.aidana.wallet_api.DTO.response;

import com.aidana.wallet_api.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;

    public UserResponse(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
    }
}
