package com.example.frases.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frases.R;
import com.example.frases.fragments.AuthorQuotesFragment;
import com.example.frases.models.Quote;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_QUOTE = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private List<Quote> quotes;
    private final OnShareClickListener shareClickListener;
    private final OnFavoriteClickListener favoriteClickListener;
    private SharedPreferences sharedPreferences;
    private List<Quote> favoriteQuotes;
    private boolean isLoading = false; //controlamos si se muestra el indicador de carga

    public interface OnShareClickListener {
        void onShareClick(Quote quote);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Quote quote, boolean isFavorite, int position);
    }

    public QuoteAdapter(List<Quote> quotes, OnShareClickListener shareListener, OnFavoriteClickListener favoriteListener, Context context) {
        this.quotes = quotes != null ? quotes : new ArrayList<>();
        this.shareClickListener = shareListener;
        this.favoriteClickListener = favoriteListener;
        this.sharedPreferences = context.getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE);
        loadFavorites();
    }

    public List<Quote> getQuotes() {
        return quotes;
    }

    public void updateQuotes(List<Quote> newQuotes) {
        this.quotes.clear();
        this.quotes.addAll(newQuotes != null ? newQuotes : new ArrayList<>());
        notifyDataSetChanged();
    }

    public void addQuotes(List<Quote> newQuotes) {
        int startPosition = this.quotes.size();
        this.quotes.addAll(newQuotes != null ? newQuotes : new ArrayList<>());
        notifyItemRangeInserted(startPosition, newQuotes != null ? newQuotes.size() : 0);
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
        notifyDataSetChanged(); //actualizamos para mostrar u ocultar el indicador de carga
    }

    @Override
    public int getItemViewType(int position) {
        return (position == quotes.size() && isLoading) ? VIEW_TYPE_LOADING : VIEW_TYPE_QUOTE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_QUOTE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quote, parent, false);
            return new QuoteViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof QuoteViewHolder) {
            Quote quote = quotes.get(position);
            QuoteViewHolder quoteHolder = (QuoteViewHolder) holder;
            quoteHolder.quoteTextView.setText(quote.getQuote());
            quoteHolder.authorTextView.setText("- " + quote.getAuthor());
            quoteHolder.shareButton.setOnClickListener(v -> shareClickListener.onShareClick(quote));
            quoteHolder.favoriteButton.setOnClickListener(v -> {
                boolean isFavorite = isFavorite(quote);
                if (isFavorite) {
                    removeFavorite(quote);
                    quoteHolder.favoriteButton.setColorFilter(null);
                    favoriteClickListener.onFavoriteClick(quote, false, position);
                    Toast.makeText(holder.itemView.getContext(), "Frase removida de favoritos", Toast.LENGTH_SHORT).show();
                } else {
                    addFavorite(quote);
                    quoteHolder.favoriteButton.setColorFilter(Color.RED);
                    favoriteClickListener.onFavoriteClick(quote, true, position);
                    Toast.makeText(holder.itemView.getContext(), "Frase añadida a favoritos", Toast.LENGTH_SHORT).show();
                }
            });

            //hacemos el autor clickable para navegar a AuthorQuotesFragment
            quoteHolder.authorTextView.setOnClickListener(v -> {
                if (holder.itemView.getContext() instanceof FragmentActivity) {
                    FragmentActivity activity = (FragmentActivity) holder.itemView.getContext();
                    AuthorQuotesFragment fragment = AuthorQuotesFragment.newInstance(quote.getAuthor());
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

            //actualizamos estado del botón de favorito
            if (isFavorite(quote)) {
                quoteHolder.favoriteButton.setColorFilter(Color.RED);
            } else {
                quoteHolder.favoriteButton.setColorFilter(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return quotes.size() + (isLoading ? 1 : 0); //añadimos 1 para el indicador de carga si isLoading es true
    }

    public void removeItem(int position) {
        if (position >= 0 && position < quotes.size()) {
            quotes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, quotes.size());
        }
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView quoteTextView, authorTextView;
        ImageButton shareButton, favoriteButton;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            quoteTextView = itemView.findViewById(R.id.quoteTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            shareButton = itemView.findViewById(R.id.shareButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
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
}