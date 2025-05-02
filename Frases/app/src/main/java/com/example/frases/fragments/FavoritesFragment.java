package com.example.frases.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frases.adapters.QuoteAdapter;
import com.example.frases.R;
import com.example.frases.models.Quote;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private QuoteAdapter quoteAdapter;
    private SharedPreferences sharedPreferences;
    private List<Quote> favoriteQuotes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        quoteAdapter = new QuoteAdapter(new ArrayList<>(), quote -> {
            //accion para compartir
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "\"" + quote.getQuote() + "\" - " + quote.getAuthor());
            startActivity(Intent.createChooser(shareIntent, "Compartir frase"));
        }, (quote, isFavorite, position) -> {
            if (!isFavorite) {
                //si se desmarca como favorito, eliminar de la lista dinámicamente
                quoteAdapter.removeItem(position);
            }
        }, getContext());
        recyclerView.setAdapter(quoteAdapter);

        sharedPreferences = requireActivity().getSharedPreferences("FavoritesPrefs", MODE_PRIVATE);
        loadFavorites();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString("favoriteQuotes", null);
        Type type = new TypeToken<List<Quote>>(){}.getType();
        favoriteQuotes = gson.fromJson(json, type);
        if (favoriteQuotes == null) {
            favoriteQuotes = new ArrayList<>();
        }

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        quoteAdapter.updateQuotes(favoriteQuotes);
    }
}