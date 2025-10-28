package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleOAuth2Service {

    @Value("${google.oauth2.client-id}")
    private String clientId;

    private GoogleIdTokenVerifier verifier;

    public GoogleOAuth2Service(@Value("${google.oauth2.client-id}") String clientId) {
        this.clientId = clientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleIdToken verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                // Verify the issuer
                if (!payload.getIssuer().equals("https://accounts.google.com") && 
                    !payload.getIssuer().equals("accounts.google.com")) {
                    return null;
                }
                return idToken;
            }
        } catch (GeneralSecurityException | IOException e) {
            System.err.println("Error verifying Google ID token: " + e.getMessage());
        }
        return null;
    }

    public String getEmailFromToken(GoogleIdToken idToken) {
        return (String) idToken.getPayload().get("email");
    }

    public String getPhoneFromToken(GoogleIdToken idToken) {
        return (String) idToken.getPayload().get("phone_number");
    }
    
    public String getAddressFromToken(GoogleIdToken idToken) {
        return (String) idToken.getPayload().get("address");
    }

    public String getNameFromToken(GoogleIdToken idToken) {
        return (String) idToken.getPayload().get("name");
    }
}
