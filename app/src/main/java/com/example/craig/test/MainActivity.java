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

        // TODO: Crap => delete
        Time time = new Time();
        EditText editTextTime = (EditText) findViewById(R.id.editTextTime);
        editTextTime.setText(time.toString());

        // TODO: Crap => delete
        /* The docs: http://square.github.io/retrofit/, don't show that you
        need to add the ConverterFactory into the call below to make things
        work in the 2.0 API.

        TODO: Figure out how to get the actual JSON out now instead of the
        callback object when I do `repos.toString()`.
        */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GithubClient service = retrofit.create(GithubClient.class);
        Call<List<Repo>> repos1 = service.listRepos("octocat");
        try {
            wait(2);
            repos1.execute();
            wait(2);
        } catch (Exception e) {};
        TextView repoText = (TextView) findViewById(R.id.repoText);
        repoText.setText(repos1.toString());

        // Calling my stuff by AsyncTask
        getRepoList();
    }

    private void getRepoList() {
        new MyAsyncTask().execute("octoman");
    }

    private class MyAsyncTask extends AsyncTask<String, Void, List<Repo>> {
        protected List<Repo> doInBackground(String... strings) {
            GithubClient client = GithubServiceGenerator.createService(GithubClient.class);
            Call<List<Repo>> call = client.listRepos(strings[0]);
            try {
                List<Repo> repos = call.execute().body();
                for (Repo repo : repos) {
                    System.out.println("repo.full_name: "+repo.full_name);
                }
                return repos;
            } catch (IOException e) {
                List<Repo> repos = null;
                return repos;
            }
        }

        protected void onPostExecute(List<Repo> result) {
            TextView repoText2 = (TextView) findViewById(R.id.repoText2);
            repoText2.setText(result.toString());
            System.out.println("end of onPostExecute."+result);
        }
    }
}