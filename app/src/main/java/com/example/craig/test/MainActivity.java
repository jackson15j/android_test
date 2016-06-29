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

import com.example.craig.test.models.fitbit.Activity;
import com.example.craig.test.models.fitbit.FitbitActivity;
import com.example.craig.test.models.fitbit.Summary;
import com.example.craig.test.models.github.GithubUser;
import com.example.craig.test.models.github.GithubUsersReposModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Properties properties = new Properties();
    String avatar_url;
    String username;
    String EXPIRY_TIME = "300";  // 604800 = 1 week.
    String FITBIT_CLIENT_ID;
    String FITBIT_CLIENT_SECRET;
    String FITBIT_REDIRECT_URI = "your://github.com/jackson15j";
    String FITBIT_USER_ID;
    String GITHUB_USER = "jackson15j";
    AccessToken FITBIT_ACCESS_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Read in client_id from local.properties under: app/src/main/assets/local.properties.
        // Note: this will NOT be committed to the git repo.
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("local.properties");
            properties.load(inputStream);
            FITBIT_CLIENT_ID = properties.getProperty("client_id");
            FITBIT_CLIENT_SECRET = properties.getProperty("client_secret");
            System.out.println("client_id: " + FITBIT_CLIENT_ID);

        } catch (IOException e) {
            e.printStackTrace();
        } // TODO: handle exception gracefully.


        // Call to update UI with hardcoded user repo list.
        getUserCompany();
        getUserContent();
        getRepoList();

        /* Button to Oauth login to Fitbit with my credentials via the
        Implicit Grant Flow (ie. no user action needed).

        The workflow is as follows:

        * User clicks button.
        * Webview (browser) loads to Fitbits Oauth request page where User enters Fitbit account.
        * On successful login we are redirected to authorization page with: Access and allow/deny.
        * Clicking allow/deny causes fitbit to send code/error appended to the redirect_uri/callback
          specified in dev.fitbit.com for the registered application. KEY POINT, the schema must be
          something unique to the application, so that the Browser/webView will not be able to parse
          it, look up in the registered intent-filters, and pass it to that application (ie. not
          http/https).
        * The redirect_uri is passed back to our application, which calls the `onResume()` block.
        * We either handle the deny, or continue with the oauth steps from the allow.

        https://dev.fitbit.com/docs/oauth2/#implicit-grant-flow.
         */
        Button fitbitLoginButton = (Button) findViewById(R.id.fitbitLoginButton);
        fitbitLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // https://www.fitbit.com/oauth2/authorize?response_type=token&client_id=22942C&redirect_uri=http%3A%2F%2Fexample.com%2Ffitbit_auth&scope=activity%20nutrition%20heartrate%20location%20nutrition%20profile%20settings%20sleep%20social%20weight&expires_in=604800
                String fitbit_auth_url =
                        "https://www.fitbit.com/oauth2/authorize?response_type=token" +
                                "&client_id=" + FITBIT_CLIENT_ID +
                                "&redirect_uri=" + FITBIT_REDIRECT_URI +
                                "&scope=activity" +
                                "&expires_in=" + EXPIRY_TIME;
                System.out.println("Fitbit Auth url: " + fitbit_auth_url);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fitbit_auth_url));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* Now that the Browser/WebView cannot handle the schema of our redirect_uri, looked up a
        registered intent-filter that points to our application, and called back to our application,
        we will be in this code block.

        Let's now figure out if the User has clicked allow/deny and take the appropriate actions.
         */
        // The intent filter defined in AndroidManifest will handle the return from
        // ACTION_VIEW intent.
        System.out.println("In onResume!!!");
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(FITBIT_REDIRECT_URI)) {
            System.out.println("redirectUri: " + uri.toString());
            // Use the parameter your API exposes for the code (mostly it's "code").
            String code = uri.getQueryParameter("code");
            if (code != null) {
                // Get access token
                System.out.println("code: " + code);
                TextView fitbitRedirectText = (TextView) findViewById(R.id.fitbitRedirectText);
                fitbitRedirectText.setText(code);
                FitbitClient fitbitClient = ApiServiceGenerator.createService(
                        FitbitClient.class,
                        FitbitClient.API_BASE_URL,
                        FITBIT_CLIENT_ID,
                        FITBIT_CLIENT_SECRET);
                Call<AccessToken> call = fitbitClient.getAccessToken(code, "authorization_code");
                try {
                    FITBIT_ACCESS_TOKEN = call.execute().body();
                    System.out.println("accessToken: " + FITBIT_ACCESS_TOKEN.getAccessToken());
                } catch (IOException e) { e.printStackTrace(); }
            } else if (uri.getQueryParameter("error") != null) {
                // Show an error message here.
                TextView fitbitRedirectText = (TextView) findViewById(R.id.fitbitRedirectText);
                fitbitRedirectText.setText(
                        uri.getQueryParameter("error") + ": " + uri.getQueryParameter("error_description"));
            } else if (uri.getFragment() != null && uri.getFragment().contains("access_token")) {
                // We have authenticated and should store the redirect url / valid access_token.

                // FIXME: Don't know why by Oauth Browser request will hang on the Browser if we
                // don't kill it between runs. Only occurs once we've clicked `allow`.
                System.out.println("Already authenticated else block uri: " + uri.toString());

                /* After the simplicity of `String value = uri.getQueryParameter("key")`, it seems
                to be an utter ballache to have to manually parse the uri, now that we have a
                fragment (schema://host#key=value&...) returned instead of a query
                (schema://host?key=value&...).

                Haven't found a better way so lets pull out the fragment and do some string munging
                to get the key/value pairs that we need.
                 */
                String fragment = uri.getFragment();
                Map<String, String> fragment_pairs = new LinkedHashMap<String, String>();
                String[] pairs = fragment.split("&");
                for (String pair : pairs) {
                    int i = pair.indexOf("=");
                    fragment_pairs.put(pair.substring(0, i), pair.substring(i + 1));
                }
                System.out.println("fragment_pairs: " + fragment_pairs.toString());
                FITBIT_USER_ID = fragment_pairs.get("user_id");
                String access_token = fragment_pairs.get("access_token");
                String token_type = fragment_pairs.get("token_type");
                FITBIT_ACCESS_TOKEN = new AccessToken(access_token, token_type);

                TextView fitbitRedirectText = (TextView) findViewById(R.id.fitbitRedirectText);
                fitbitRedirectText.setText(
                        "user_id: " + FITBIT_USER_ID + ", access_token: " + access_token);
            }
            /* This is a super big bodge.

            onResume() is called when loading the app. Which means if I don't handle arguments
            like user_id being null, then we'll blow up if this is moved to be called outside
            of the Fitbit OAuth code above.

            Super dirty, but good enough to get it working at least.

            TODO: Fix the issue where we have not authenticated & have null arguments required
            to do authenticated API calls.
             */
            getFitbitStepsAsyncTask();
        }
    }

    /* Call to get RepoList.

     This the method that is called from the UI thread, to then spawn up a
     separate thread which the network call is made within.
     */
    private void getUserContent() {
        new populateUserAsyncTask().execute(GITHUB_USER);
    }

    private void getRepoList() {
        new MyAsyncTask().execute(GITHUB_USER);
    }

    private void getFitbitStepsAsyncTask() { new populateFitbitActivityAsyncTask().execute(); }

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
            } catch (IOException e) {
            } // TODO: deal with exception.
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
                System.out.println("repo.full_name: " + repo.getFullName());
                repoList.setText(repoList.getText() + repo.getFullName() + "\n");
            }
            System.out.println("end of onPostExecute." + result);
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
                System.out.println("Saving image url: " + avatar_url);
            } catch (IOException e) {
            } // TODO: deal with exception.
            return githubUser;
        }

        protected void onPostExecute(GithubUser result) {
            TextView nameText = (TextView) findViewById(R.id.nameText);
            nameText.setText(username);


            // lets pull in the github avatar with http://square.github.io/picasso/.
            System.out.println("Attempting to load image from: " + avatar_url);
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
                if (response == null) {
                    System.out.println("What is causing a null Github response?? " + response);
                    return;
                }
                System.out.println("response body: " + response.body());
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

    private class populateFitbitActivityAsyncTask extends AsyncTask<Void, Void, FitbitActivity> {
        protected FitbitActivity doInBackground(Void... params) {
            System.out.println("in populateFitbitActivityAsyncTask()...");
            // Fitbit query needs date in: yyyy-MM-dd, format
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String todays_date = dateFormat.format(date);

            FitbitActivity fitbitActivity = null;
            FitbitClient client = ApiServiceGenerator.createService(
                    FitbitClient.class,
                    FitbitClient.API_BASE_URL,
                    FITBIT_ACCESS_TOKEN);
            Call<FitbitActivity> fitbitActivityCall = client.getActivity(FITBIT_USER_ID, todays_date);
            try {
                fitbitActivity = fitbitActivityCall.execute().body();
                Summary summary = fitbitActivity.getSummary();
            } catch (IOException e) {
                e.printStackTrace();
            } // TODO: deal with exception.
            return fitbitActivity;
        }

        protected void onPostExecute(FitbitActivity result) {
            final TextView fitbitStepsText = (TextView) findViewById(R.id.fitbitStepsText);
            int steps = result.getSummary().getSteps();
            fitbitStepsText.setText("Steps: " + steps);
            System.out.println("Fitbit Steps: " + steps);
        }
    }
}