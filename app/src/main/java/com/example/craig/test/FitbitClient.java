package com.example.craig.test;

import com.example.craig.test.models.fitbit.FitbitActivity;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by craig on 28/06/16.
 */
public interface FitbitClient {
    public static final String API_BASE_URL = "https://api.fitbit.com";

//    @FormUrlEncoded
//    @POST("/oauth2/authorization")

    @FormUrlEncoded
    @POST("/oauth2/token")
    Call<AccessToken> getAccessToken(
            @Field("code") String code,
            @Field("grant_type") String grantType);
}
