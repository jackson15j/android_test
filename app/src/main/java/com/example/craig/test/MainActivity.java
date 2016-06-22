package com.example.craig.test;

import android.app.Dialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity {

    TextView repoText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView someText = (TextView) findViewById(R.id.myString);
        someText.setText("jam");

        // Calling my stuff by AsyncTask
        getRepoList();
    }

    private void getRepoList() {
        new MyAsyncTask().execute("jackson15j");
    }

    private class MyAsyncTask extends AsyncTask<String, Void, List<GithubUsersReposModel>> {
        protected List<GithubUsersReposModel> doInBackground(String... strings) {
            GithubClient client = GithubServiceGenerator.createService(GithubClient.class);
            Call<List<GithubUsersReposModel>> call = client.listRepos(strings[0]);
            try {
                List<GithubUsersReposModel> repos = call.execute().body();
                for (GithubUsersReposModel repo : repos) {
                    System.out.println("repo.full_name: "+repo.getFullName());
                }
                return repos;
            } catch (IOException e) {
                List<GithubUsersReposModel> repos = null;
                return repos;
            }
        }

        protected void onPostExecute(List<GithubUsersReposModel> result) {
            TextView repoText2 = (TextView) findViewById(R.id.repoText2);
            repoText2.setText(result.toString());
            System.out.println("end of onPostExecute."+result);
        }
    }
}