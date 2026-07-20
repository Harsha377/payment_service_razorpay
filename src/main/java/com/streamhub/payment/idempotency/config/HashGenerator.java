package com.streamhub.payment.idempotency.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamhub.payment.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    private final ObjectMapper objectMapper;

    public String generateSha256(Object object){
        try{
           String json= objectMapper.writeValueAsString(object);
           MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");
           byte[] hash=messageDigest.digest(json.getBytes(StandardCharsets.UTF_8));
           StringBuilder hexString=new StringBuilder();
           for (byte b: hash){
               hexString.append(String.format("%02x",b));
           }
           return hexString.toString();
        }
        catch (JsonProcessingException e){
            throw new ApiException("Failed to serialize request for hashing.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (NoSuchAlgorithmException e){
            throw new ApiException("SHA-256 algorithm is not available.",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
