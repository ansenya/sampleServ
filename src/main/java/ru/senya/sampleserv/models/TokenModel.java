package ru.senya.sampleserv.models;

import lombok.Data;

@Data
public class TokenModel {
    String iamToken, expiresAt;
}
