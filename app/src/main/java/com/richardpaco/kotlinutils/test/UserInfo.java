package com.richardpaco.kotlinutils.test;

import java.io.Serializable;

/**
 * Author: Richard paco
 * Date: 2018/6/27
 * Desc:
 */
public class UserInfo implements Serializable {

    private String username;
    private String password;

    public UserInfo() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
