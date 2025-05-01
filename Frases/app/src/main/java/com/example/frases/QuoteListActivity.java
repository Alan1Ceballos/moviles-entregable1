package com.example.frases;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.frases.adapters.QuoteAdapter;
import com.example.frases.models.Quote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuoteListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuoteAdapter adapter;
    private ArrayList<Quote> quoteList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_list);

        recyclerView = findViewById(R.id.recyclerViewQuotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuoteAdapter(quoteList);
        recyclerView.setAdapter(adapter);

        fetchQuotes();
    }

    private void fetchQuotes() {
        Request request = new Request.Builder()
                .url("https://dummyjson.com/quotes?limit=100") // Solicita 100 frases
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(QuoteListActivity.this, "Error de red", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) return;

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray quotesArray = jsonObject.getJSONArray("quotes");

                    for (int i = 0; i < quotesArray.length(); i++) {
                        JSONObject q = quotesArray.getJSONObject(i);
                        String quoteText = q.getString("quote");
                        String author = q.getString("author");

                        Quote quote = new Quote(quoteText, author);
                        quoteList.add(quote);
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
