package com.example.frases.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.frases.R;
import com.example.frases.api.ApiClient;
import com.example.frases.api.QuoteApiService;
import com.example.frases.models.Quote;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuoteFragment extends Fragment {

    private TextView quoteTextView, authorTextView;
    private MaterialButton newQuoteButton;
    private ImageButton shareButton, favoriteButton;
    private ProgressBar progressBar;
    private QuoteApiService apiService;
    private Quote currentQuote;
    private SharedPreferences sharedPreferences;
    private List<Quote> favoriteQuotes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quote, container, false);

        quoteTextView = view.findViewById(R.id.quoteTextView);
        authorTextView = view.findViewById(R.id.authorTextView);
        newQuoteButton = view.findViewById(R.id.newQuoteButton);
        shareButton = view.findViewById(R.id.shareButton);
        favoriteButton = view.findViewById(R.id.favoriteButton);
        progressBar = view.findViewById(R.id.progressBar);

        sharedPreferences = requireActivity().getSharedPreferences("FavoritesPrefs", MODE_PRIVATE);
        loadFavorites();

        apiService = ApiClient.getClient().create(QuoteApiService.class);

        //cargamos la frase inicial
        loadRandomQuote();

        newQuoteButton.setOnClickListener(v -> {
            Animation scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
            newQuoteButton.startAnimation(scaleUp);

            //aplicamos animación de salida a la frase actual
            Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //una vez que la animación de salida termina, cargamos la nueva frase
                    loadRandomQuote();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            quoteTextView.startAnimation(fadeOut);
            authorTextView.startAnimation(fadeOut);
        });

        shareButton.setOnClickListener(v -> {
            if (currentQuote != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "\"" + currentQuote.getQuote() + "\" - " + currentQuote.getAuthor());
                startActivity(Intent.createChooser(shareIntent, "Compartir frase"));
            } else {
                Toast.makeText(getContext(), "No hay frase para compartir", Toast.LENGTH_SHORT).show();
            }
        });

        favoriteButton.setOnClickListener(v -> {
            if (currentQuote != null) {
                if (isFavorite(currentQuote)) {
                    removeFavorite(currentQuote);
                    favoriteButton.setColorFilter(null);
                    Toast.makeText(getContext(), "Frase removida de favoritos", Toast.LENGTH_SHORT).show();
                } else {
                    addFavorite(currentQuote);
                    favoriteButton.setColorFilter(getResources().getColor(android.R.color.holo_red_dark, null));
                    Toast.makeText(getContext(), "Frase añadida a favoritos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void loadRandomQuote() {
        progressBar.setVisibility(View.VISIBLE);
        quoteTextView.setVisibility(View.GONE);
        authorTextView.setVisibility(View.GONE);

        Call<Quote> call = apiService.getRandomQuote();
        call.enqueue(new Callback<Quote>() {
            @Override
            public void onResponse(Call<Quote> call, Response<Quote> response) {
                if (getActivity() != null) {
                    progressBar.setVisibility(View.GONE);
                    quoteTextView.setVisibility(View.VISIBLE);
                    authorTextView.setVisibility(View.VISIBLE);

                    if (response.isSuccessful() && response.body() != null) {
                        currentQuote = response.body();
                        quoteTextView.setText(currentQuote.getQuote());
                        authorTextView.setText("- " + currentQuote.getAuthor());

                        //actualizamos el estado del botón de favorito
                        updateFavoriteButton();

                        //aplicamos animación de entrada a la nueva frase
                        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                        quoteTextView.startAnimation(fadeIn);
                        authorTextView.startAnimation(fadeIn);
                    } else {
                        Toast.makeText(getContext(), "Error al cargar la frase", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Quote> call, Throwable t) {
                if (getActivity() != null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadFavorites() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("favoriteQuotes", null);
        Type type = new TypeToken<List<Quote>>(){}.getType();
        favoriteQuotes = gson.fromJson(json, type);
        if (favoriteQuotes == null) {
            favoriteQuotes = new ArrayList<>();
        }
    }

    private void saveFavorites() {
        Gson gson = new Gson();
        String json = gson.toJson(favoriteQuotes);
        sharedPreferences.edit().putString("favoriteQuotes", json).apply();
    }

    private void addFavorite(Quote quote) {
        if (!isFavorite(quote)) {
            favoriteQuotes.add(quote);
            saveFavorites();
        }
    }

    private void removeFavorite(Quote quote) {
        favoriteQuotes.removeIf(q -> q.getId() == quote.getId());
        saveFavorites();
    }

    private boolean isFavorite(Quote quote) {
        return favoriteQuotes.stream().anyMatch(q -> q.getId() == quote.getId());
    }

    private void updateFavoriteButton() {
        if (currentQuote != null && isFavorite(currentQuote)) {
            favoriteButton.setColorFilter(getResources().getColor(android.R.color.holo_red_dark, null));
        } else {
            favoriteButton.setColorFilter(null);
        }
    }
}