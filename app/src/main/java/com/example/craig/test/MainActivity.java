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
    String username;
    String GITHUB_USER = "jackson15j";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Call to update UI with hardcoded user repo list.
        getUserContent();
        getRepoList();
    }

    /* Call to get RepoList.

     This the method that is called from the UI thread, to then spawn up a
     separate thread which the network call is made within.
     */
    private void getUserContent() { new populateUserAsyncTask().execute(GITHUB_USER); }
    private void getRepoList() { new MyAsyncTask().execute(GITHUB_USER); }

    /* Call to handle Network Call thread.

     We Cannot make any calls in the UI Thread that are unrelated to UI.
     So lets use AsynTask to do our network call in, and update the UI
     when the result has returned.
     */
    private class MyAsyncTask extends AsyncTask<String, Void, List<GithubUsersReposModel>> {
        /* Does network call in background thread.

        This contains our network call, but it should also contain any additional work that is not
        UI based. The reason for this is that this is all done in a non-UI background thread.
         */
        protected List<GithubUsersReposModel> doInBackground(String... strings) {
            List<GithubUsersReposModel> repos = null;
            GithubClient client = GithubServiceGenerator.createService(GithubClient.class);
            // lets temporarily add in a getUser API call here, before refactoring
            // to allow multiple calls to be made via an AsyncTask broker.
            // TODO: refactor so I can do different API calls.
            Call<List<GithubUsersReposModel>> call = client.listRepos(strings[0]);
            try {
                repos = call.execute().body();
            } catch (IOException e) {} // TODO: deal with exception.
            return repos;
        }

        /* Does UI update with result from `doInBackground()`.

        This should contain the minimal amount of work to display the reseult onto the UI. Any
        non-UI work should be done in the `doInBackground()` stage since if done here, it will
        potentially block the UI to the point that is visible to the user as a "laggy" UI.
         */
        protected void onPostExecute(List<GithubUsersReposModel> result) {
            TextView repoText2 = (TextView) findViewById(R.id.repoText2);
            repoText2.setText(result.toString());

            // pull out repos to a list for displaying in UI.
            TextView repoList = (TextView) findViewById(R.id.repoList);
            for (GithubUsersReposModel repo : result) {
                System.out.println("repo.full_name: "+repo.getFullName());
                repoList.setText(repoList.getText() + repo.getFullName() + "\n");
            }
            System.out.println("end of onPostExecute."+result);
        }
    }

    /* User network calls AsyncTask */
    private class populateUserAsyncTask extends AsyncTask<String, Void, GithubUser> {
        protected GithubUser doInBackground(String... strings) {
            GithubUser githubUser = null;
            GithubClient client = GithubServiceGenerator.createService(GithubClient.class);
            Call<GithubUser> githubUserCall = client.getUser(strings[0]);
            try {
                githubUser = githubUserCall.execute().body();
                avatar_url = githubUser.getAvatarUrl();
                username = githubUser.getName();
                System.out.println("Saving image url: "+avatar_url);
            } catch (IOException e) {} // TODO: deal with exception.
            return githubUser;
        }

        protected void onPostExecute(GithubUser result) {
            TextView nameText = (TextView) findViewById(R.id.nameText);
            nameText.setText(username);


            // lets pull in the github avatar with http://square.github.io/picasso/.
            System.out.println("Attempting to load image from: "+avatar_url);
            ImageView githubPhoto = (ImageView) findViewById(R.id.githubPhoto);
            Picasso.with(getApplicationContext()).load(avatar_url).into(githubPhoto);
        }
    }
}