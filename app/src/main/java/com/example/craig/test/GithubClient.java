package com.example.craig.test;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by craig on 21/06/16.
 */

public interface GithubClient
{
    @GET("/users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);
}

class Repo
{
    String full_name;
}