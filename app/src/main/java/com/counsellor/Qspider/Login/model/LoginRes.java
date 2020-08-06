package com.counsellor.Qspider.Login.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginRes {

@SerializedName("auth_token")
@Expose
private String authToken;

public String getAuthToken() {
return authToken;
}

public void setAuthToken(String authToken) {
this.authToken = authToken;
}

}