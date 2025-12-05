package org.example.miniecommerce.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
  ADMIN,
  CUSTOMER,
  STAFF;
    @JsonCreator
    public static Role fromString(String value) {
        return Role.valueOf(value.toUpperCase());
    }
}
