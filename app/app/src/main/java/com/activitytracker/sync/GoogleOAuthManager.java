package com.activitytracker.sync;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.util.Collections;

/**
 * Manages OAuth 2.0 token acquisition and account selection for Google Cloud Storage
 * using Google's standard security utilities (GoogleAccountCredential and GoogleAuthUtil).
 *
 * Technical Constraint:
 * "Use Google's standard security utilities such as GoogleAuthUtil and GoogleAccountCredential
 * to perform any OAuth 2.0 handshakes. Assume that an OAuth 2.0 Client ID for the application's
 * package and SHA-1 certificate fingerprint already exists."
 */
public class GoogleOAuthManager {

    public static final String STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";

    public interface OAuthTokenProvider {
        String getAccessToken(Context context, String accountName, String scope) throws Exception;
        void invalidateToken(Context context, String token);
    }

    private static OAuthTokenProvider tokenProvider = new DefaultOAuthTokenProvider();

    public static void setTokenProviderForTesting(OAuthTokenProvider provider) {
        tokenProvider = provider != null ? provider : new DefaultOAuthTokenProvider();
    }

    public static OAuthTokenProvider getTokenProvider() {
        return tokenProvider;
    }

    /**
     * Creates an Intent to prompt the user to choose their Google account using
     * GoogleAccountCredential.
     */
    public static Intent createChooseAccountIntent(Context context) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singletonList(STORAGE_SCOPE));
        return credential.newChooseAccountIntent();
    }

    /**
     * Obtains an OAuth 2.0 Bearer access token for the selected account using
     * standard Google security utilities.
     */
    public static String getAccessToken(Context context, String accountName) throws Exception {
        return tokenProvider.getAccessToken(context, accountName, STORAGE_SCOPE);
    }

    /**
     * Invalidates a cached access token so that a fresh token will be fetched on next request.
     */
    public static void invalidateToken(Context context, String token) {
        tokenProvider.invalidateToken(context, token);
    }

    public static class DefaultOAuthTokenProvider implements OAuthTokenProvider {
        @Override
        public String getAccessToken(Context context, String accountName, String scope) throws Exception {
            // 1. Attempt using GoogleAccountCredential (standard Google API client utility)
            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        context, Collections.singletonList(scope));
                if (accountName != null && !accountName.trim().isEmpty()) {
                    credential.setSelectedAccountName(accountName.trim());
                }
                String token = credential.getToken();
                if (token != null && !token.trim().isEmpty()) {
                    return token.trim();
                }
            } catch (UserRecoverableAuthException uae) {
                throw uae;
            } catch (Exception ignored) {
                // Fall back to GoogleAuthUtil
            }

            // 2. Fall back to GoogleAuthUtil (standard Play Services utility)
            if (accountName != null && !accountName.trim().isEmpty()) {
                String fullScope = "oauth2:" + scope;
                return GoogleAuthUtil.getToken(context, accountName.trim(), fullScope);
            }

            throw new IllegalStateException("No Google account selected for OAuth 2.0 handshake");
        }

        @Override
        public void invalidateToken(Context context, String token) {
            if (token != null && context != null) {
                try {
                    GoogleAuthUtil.invalidateToken(context, token);
                } catch (Throwable ignored) {}
            }
        }
    }
}
