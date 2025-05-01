package com.example.frases;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.frases.fragments.QuoteFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new QuoteFragment())
                    .commit();
        }
    }
}
