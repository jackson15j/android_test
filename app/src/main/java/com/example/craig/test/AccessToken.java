package com.example.craig.test;

/**
 * Created by craig on 27/06/16.
 *
 * Used as part of Oauth login with a token.
 *
 * https://futurestud.io/blog/oauth-2-on-android-with-retrofit.
 */
public class AccessToken {
    private String accessToken;
    private String tokenType;

    public String getAccessToken() { return accessToken; }

    public String getTokenType() {
        // OAuth requires uppercase Authorization HTTP header value for token type.
        if ( ! Character.isUpperCase(tokenType.charAt(0))) {
            tokenType = Character
                    .toString(tokenType.charAt(0))
                    .toUpperCase() + tokenType.substring(1);
        }
        return tokenType;
    }
}
