package com.example.frases.api;

import com.example.frases.models.Quote;
import com.example.frases.models.QuoteListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QuoteApiService {
    @GET("quotes/random")
    Call<Quote> getRandomQuote();

    @GET("quotes")
    Call<QuoteListResponse> getAllQuotes();

    @GET("quotes")
    Call<QuoteListResponse> getAllQuotesWithPagination(
            @Query("limit") int limit,
            @Query("skip") int skip
    );
}