package com.example.craig.test;

import com.example.craig.test.models.github.GithubUser;
import com.example.craig.test.models.github.GithubUsersReposModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * A Github Client which has the API for calls made on top of
 * a ApiServiceGenerator instance.
 */
public interface GithubClient
{
    public static final String API_BASE_URL = "https://api.github.com";

    @GET("/users/{user}")
    Call<GithubUser> getUser(@Path("user") String user);

    @GET("/users/{user}/repos")
    Call<List<GithubUsersReposModel>> listRepos(@Path("user") String user);
}