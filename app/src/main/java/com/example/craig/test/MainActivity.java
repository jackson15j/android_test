package com.example.craig.test;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    String avatar_url;
    TextView repoText2;
    TextView repoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView someText = (TextView) findViewById(R.id.myString);
        someText.setText("jam");

        // Call to update UI with hardcoded user repo list.
        getRepoList();
    }

    /* Call to get RepoList.

     This the method that is called from the UI thread, to then spawn up a
     separate thread which the network call is made within.
     */
    private void getRepoList() {
        new MyAsyncTask().execute("jackson15j");
    }

    /* Call to handle Network Call thread.

     We Cannot make any calls in the UI Thread that are unrelated to UI.
     So lets use AsynTask to do our network call in, and update the UI
     when the result has returned.
     */
    private class MyAsyncTask extends AsyncTask<String, Void, List<GithubUsersReposModel>> {
        /* Does network call in background thread. */
        protected List<GithubUsersReposModel> doInBackground(String... strings) {
            GithubClient client = GithubServiceGenerator.createService(GithubClient.class);
            // lets temporarily add in a getUser API call here, before refactoring
            // to allow multiple calls to be made via an AsyncTask broker.
            // TODO: refactor so I can do different API calls.
            Call<GithubUser> githubUserCall = client.getUser(strings[0]);
            Call<List<GithubUsersReposModel>> call = client.listRepos(strings[0]);
            try {
                GithubUser githubUser = githubUserCall.execute().body();
                System.out.println("Name: "+githubUser.getName()+", Blog: "+githubUser.getBlog());
                avatar_url = githubUser.getAvatarUrl();
                System.out.println("Saving image url: "+avatar_url+", "+githubUser.getAvatarUrl());

                List<GithubUsersReposModel> repos = call.execute().body();
                return repos;
            } catch (IOException e) {
                List<GithubUsersReposModel> repos = null;
                return repos;
            }
        }

        /* Does UI update with result from `doInBackground()`. */
        protected void onPostExecute(List<GithubUsersReposModel> result) {
            // lets pull in the github avatar with http://square.github.io/picasso/.
            System.out.println("Attempting to load image from: "+avatar_url);
            ImageView githubPhoto = (ImageView) findViewById(R.id.githubPhoto);
            Picasso.with(getApplicationContext()).load(avatar_url).into(githubPhoto);

            TextView repoText2 = (TextView) findViewById(R.id.repoText2);
            repoText2.setText(result.toString());

            // pull out repos to a list for displaying in UI.
            List<String> repo_list = new ArrayList<String>();
            for (GithubUsersReposModel repo : result) {
                System.out.println("repo.full_name: "+repo.getFullName());
                repo_list.add(repo.getFullName());
            }
            TextView repoList = (TextView) findViewById(R.id.repoList);
            repoList.setText(repo_list.toString());
            System.out.println("end of onPostExecute."+result);
        }
    }
}