package com.example.craig.test;

import android.util.Base64;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
        return createService(serviceClass, apiBaseUrl, null);
    }

    /* Basic Auth version of createService().

    https://futurestud.io/blog/android-basic-authentication-with-retrofit.
    */
    public static <S> S createService(
            Class<S> serviceClass,
            String apiBaseUrl,
            String username,
            String password) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create());

        if (username != null && password != null) {
            System.out.println("Attempting to create ApiServiceGenerator with username/password for Basic auth...");
            String credentials = username + ":" + password;
            final String basic = "Basic " + Base64.encodeToString(
                    credentials.getBytes(), Base64.NO_WRAP);
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", basic)
                            .header("Accept", "application/json")
                            .method(original.method(), original.body());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    /* Oauth version of createService().

    https://futurestud.io/blog/oauth-2-on-android-with-retrofit.
     */
    public static <S> S createService(Class<S> serviceClass, String apiBaseUrl, final AccessToken token) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create());

        if (token != null) {
            System.out.println("Attempting to create ApiServiceGenerator with token for Oauth...");
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Authorization", token.getTokenType() + " " + token.getAccessToken())
                            .method(original.method(), original.body());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }
}