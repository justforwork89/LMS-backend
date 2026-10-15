package com.dy.lms.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {
    private String userId;
    private String password;
    private String userName;
    private String roleCd;
    private String major;
    private String useYn;

    public Role getRole() { return Role.fromCode(roleCd); }

    public boolean isActive() { return "Y".equals(useYn); }
}