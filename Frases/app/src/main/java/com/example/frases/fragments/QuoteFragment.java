package com.example.frases.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frases.QuoteListActivity;
import com.example.frases.R;
import com.example.frases.api.QuoteApi;

import org.json.JSONException;

public class QuoteFragment extends Fragment {

    private TextView quoteTextView;
    private TextView authorTextView;
    private Button refreshButton;
    private QuoteApi quoteApi;

    public QuoteFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quote, container, false);

        quoteTextView = view.findViewById(R.id.quoteTextView);
        authorTextView = view.findViewById(R.id.authorTextView);
        refreshButton = view.findViewById(R.id.refreshButton);

        quoteApi = new QuoteApi(requireContext());

        loadRandomQuote();

        refreshButton.setOnClickListener(v -> loadRandomQuote());

        Button openListButton = view.findViewById(R.id.openListButton);
        openListButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuoteListActivity.class);
            startActivity(intent);
        });


        return view;
    }

    private void loadRandomQuote() {
        quoteApi.getRandomQuote(response -> {
            try {
                String text = response.getString("quote");
                String author = response.getString("author");

                quoteTextView.setText("\"" + text + "\"");
                authorTextView.setText("- " + author);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(getContext(), "Error al cargar frase", Toast.LENGTH_SHORT).show());
    }
}
