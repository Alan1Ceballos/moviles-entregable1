package com.example.frases.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.frases.R;
import com.example.frases.models.Quote;

import java.util.List;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder> {

    private List<Quote> quotes;

    public QuoteAdapter(List<Quote> quotes) {
        this.quotes = quotes;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView quoteText;
        public TextView authorText;

        public ViewHolder(View view) {
            super(view);
            quoteText = view.findViewById(R.id.quoteText);
            authorText = view.findViewById(R.id.authorText);
        }
    }

    @NonNull
    @Override
    public QuoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quote, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteAdapter.ViewHolder holder, int position) {
        Quote quote = quotes.get(position);
        holder.quoteText.setText(quote.getQuote());
        holder.authorText.setText("- " + quote.getAuthor());
    }

    @Override
    public int getItemCount() {
        return quotes.size();
    }
}
