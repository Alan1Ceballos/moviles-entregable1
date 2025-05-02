package com.example.frases.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuotesListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private QuoteApiService apiService;
    private QuoteAdapter quoteAdapter;
    private int skip = 0; //para la paginación
    private final int limit = 30; //cantidad de frases por página
    private boolean isLoading = false; //evitamos múltiples cargas simultáneas
    private boolean hasMoreQuotes = true; //veriicamos si hay más frases

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quotes_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

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

        apiService = ApiClient.getClient().create(QuoteApiService.class);

        //configuramos el listener para carga automática
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && hasMoreQuotes) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    //cargamos más si estamos cerca del final (a 5 elementos del final)
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0) {
                        skip += limit; //incrementamos el offset para la siguiente página
                        loadQuotes();
                    }
                }
            }
        });

        //cargamos las primeras frases
        loadQuotes();

        return view;
    }

    private void loadQuotes() {
        if (isLoading || !hasMoreQuotes) return;
        isLoading = true;

        //mostramos ProgressBar central solo en la carga inicial
        if (skip == 0) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            quoteAdapter.setLoading(true); //mostramos indicador de carga al final
        }

        Call<QuoteListResponse> call = apiService.getAllQuotesWithPagination(limit, skip);
        call.enqueue(new Callback<QuoteListResponse>() {
            @Override
            public void onResponse(Call<QuoteListResponse> call, Response<QuoteListResponse> response) {
                if (getActivity() != null) {
                    progressBar.setVisibility(View.GONE);
                    quoteAdapter.setLoading(false); //ocultamos indicador de carga
                    isLoading = false;

                    if (response.isSuccessful() && response.body() != null) {
                        List<Quote> quotes = response.body().getQuotes();
                        if (quotes.isEmpty()) {
                            hasMoreQuotes = false;
                            Toast.makeText(getContext(), "No hay más frases", Toast.LENGTH_SHORT).show();
                        } else {
                            quoteAdapter.addQuotes(quotes);
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
                    quoteAdapter.setLoading(false);
                    isLoading = false;
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}