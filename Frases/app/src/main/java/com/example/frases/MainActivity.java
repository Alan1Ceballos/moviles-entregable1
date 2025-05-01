package com.example.frases;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.frases.fragments.QuoteFragment;
import android.widget.Button;
import android.content.Intent;
import android.widget.Button;


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
