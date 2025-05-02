package com.example.frases.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frases.adapters.QuoteAdapter;
import com.example.frases.R;
import com.example.frases.api.ApiClient;
import com.example.frases.api.QuoteApiService;
import com.example.frases.models.Quote;
import com.example.frases.models.QuoteListResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.button.MaterialButton;

public class AuthorQuotesFragment extends Fragment {

    private TextView authorTitleTextView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MaterialButton backButton;
    private QuoteAdapter quoteAdapter;
    private QuoteApiService apiService;
    private String authorName;

    private static final String ARG_AUTHOR_NAME = "author_name";

    public static AuthorQuotesFragment newInstance(String authorName) {
        AuthorQuotesFragment fragment = new AuthorQuotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AUTHOR_NAME, authorName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_author_quotes, container, false);

        authorTitleTextView = view.findViewById(R.id.authorTitleTextView);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        backButton = view.findViewById(R.id.backButton); //inicializamos el botón

        if (getArguments() != null) {
            authorName = getArguments().getString(ARG_AUTHOR_NAME);
            authorTitleTextView.setText("Frases de " + authorName);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        quoteAdapter = new QuoteAdapter(new ArrayList<>(), quote -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "\"" + quote.getQuote() + "\" - " + quote.getAuthor());
            startActivity(Intent.createChooser(shareIntent, "Compartir frase"));
        }, (quote, isFavorite, position) -> {
            //no hacemos nada con la lista aquí, solo actualizamos el botón
        }, getContext());
        recyclerView.setAdapter(quoteAdapter);

        //condiguramos el botón de regresar
        backButton.setOnClickListener(v -> {
            if (getActivity() != null && getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getActivity().getSupportFragmentManager().popBackStack(); //vamos hacia atrás
            }
        });

        apiService = ApiClient.getClient().create(QuoteApiService.class);
        loadQuotesByAuthor();

        return view;
    }

    private void loadQuotesByAuthor() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        Call<QuoteListResponse> call = apiService.getAllQuotesWithPagination(100, 0);
        call.enqueue(new Callback<QuoteListResponse>() {
            @Override
            public void onResponse(Call<QuoteListResponse> call, Response<QuoteListResponse> response) {
                if (getActivity() != null) {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (response.isSuccessful() && response.body() != null) {
                        List<Quote> allQuotes = response.body().getQuotes();

                        //filtrar por autor
                        List<Quote> authorQuotes = allQuotes.stream()
                                .filter(quote -> quote.getAuthor().equalsIgnoreCase(authorName))
                                .collect(Collectors.toList());

                        //seleccionamos 10 frases aleatorias
                        List<Quote> randomQuotes = getRandomQuotes(authorQuotes, 10);

                        //mostramos las frases
                        quoteAdapter.updateQuotes(randomQuotes);

                        if (randomQuotes.isEmpty()) {
                            Toast.makeText(getContext(), "No se encontraron frases de " + authorName, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error al cargar las frases", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<QuoteListResponse> call, Throwable t) {
                if (getActivity() != null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<Quote> getRandomQuotes(List<Quote> quotes, int count) {
        if (quotes.size() <= count) {
            return new ArrayList<>(quotes); //si hay menos de 10, devolver todas
        }
        List<Quote> shuffledQuotes = new ArrayList<>(quotes);
        Collections.shuffle(shuffledQuotes); //mezclamos aleatoriamente
        return shuffledQuotes.subList(0, Math.min(count, shuffledQuotes.size())); //tomamos las primeras 10
    }
}