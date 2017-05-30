/**
 * Copyright 2016 IMONT Technologies
 * Created by romanas on 31/08/2016.
 */
package io.imont.android.sdkdemo.cloud;

import java.io.Serializable;

public class CloudAccount implements Serializable {
    private String token;
    private String providerSpecificId;
    private String firstName;
    private String lastName;
    private String email;

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getProviderSpecificId() {
        return providerSpecificId;
    }

    public void setProviderSpecificId(final String providerSpecificId) {
        this.providerSpecificId = providerSpecificId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "CloudAccount{" +
                "token='" + token + '\'' +
                ", providerSpecificId='" + providerSpecificId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
