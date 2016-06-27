package com.example.craig.test;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    String avatar_url;
    String username;
    String GITHUB_USER = "jackson15j";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Call to update UI with hardcoded user repo list.
        getUserCompany();
        getUserContent();
        getRepoList();

        /* Button to Oauth login to Fitbit with my credentials via the
        Implicit Grant Flow (ie. no user action needed).

        https://dev.fitbit.com/docs/oauth2/#implicit-grant-flow.
         */
        Button fitbitLoginButton = (Button) findViewById(R.id.fitbitLoginButton);
        fitbitLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Read in client_id from local.properties under: app/src/main/assets/local.properties.
                // Note: this will NOT be committed to the git repo.
                Properties properties = new Properties();
                try {
                    AssetManager assetManager = getAssets();
                    InputStream inputStream = assetManager.open("local.properties");
                    properties.load(inputStream);
                    System.out.println("client_id: " + properties.getProperty("client_id"));
                } catch (IOException e) { e.printStackTrace(); } // TODO: handle exception gracefully.


                // https://www.fitbit.com/oauth2/authorize?response_type=token&client_id=22942C&redirect_uri=http%3A%2F%2Fexample.com%2Ffitbit_auth&scope=activity%20nutrition%20heartrate%20location%20nutrition%20profile%20settings%20sleep%20social%20weight&expires_in=604800
                String fitbit_auth_url =
                        "https://www.fitbit.com/oauth2/authorize?response_type=token" +
                                "&client_id=" + properties.getProperty("client_id") +
                                "&redirect_uri=https://github.com/jackson15j" +
                                "&scope=activity" +
                                "&expires_in=604800";
                System.out.println("Fitbit Auth url: " + fitbit_auth_url);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fitbit_auth_url));
                startActivity(intent);
            }
        });
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
            GithubClient client = ApiServiceGenerator.createService(
                    GithubClient.class,
                    GithubClient.API_BASE_URL);
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
            // pull out repos to a list for displaying in UI.
            TextView repoList = (TextView) findViewById(R.id.repoList);
            repoList.setText("Repos: \n\n");
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
            GithubClient client = ApiServiceGenerator.createService(
                    GithubClient.class,
                    GithubClient.API_BASE_URL);
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

    /* Gets the Users company info.

    I've grabbed the prior api calls via AsyncTask, but lets hack something together
    using Retrofit2's async method.

    Note: This (I believe) is done all in the main UI thread, so some sadness.
     */
    private void getUserCompany() {
        final TextView textView = (TextView) findViewById(R.id.textView);
        GithubClient client = ApiServiceGenerator.createService(
                GithubClient.class,
                GithubClient.API_BASE_URL);
        Call<GithubUser> call = client.getUser(GITHUB_USER);
        call.enqueue(new Callback<GithubUser>() {
            @Override
            public void onResponse(Call call, Response response) {
                System.out.println("response body: "+response.body());
                GithubUser githubUser = (GithubUser) response.body();
                System.out.println("Getting company: " + githubUser.getCompany());
                textView.setText(githubUser.getCompany());
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                System.out.println("callback failed for call: " + call + ", " + t);
            } // TODO: Handle failure case.
        });
    }
}