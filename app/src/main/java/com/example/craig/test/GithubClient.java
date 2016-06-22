package com.example.craig.test;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * A Github Client which has the API for calls made on top of
 * a GithubServiceGenerator instance.
 */
public interface GithubClient
{
    @GET("/users/{user}/repos")
    Call<List<GithubUsersReposModel>> listRepos(@Path("user") String user);
}