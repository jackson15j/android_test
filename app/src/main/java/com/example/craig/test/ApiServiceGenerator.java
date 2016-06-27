package com.example.craig.test;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A generic Service instance that you can make API calls with.
 *
 * https://futurestud.io/blog/retrofit-getting-started-and-android-client.
 *
 * You need to pass in the API's base url!
 */
public class ApiServiceGenerator {
    public static <S> S createService(Class<S> serviceClass, String apiBaseUrl) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}