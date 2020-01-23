package com.example.easygardening;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    String[] Array = {"Fruit", "Vegetable", "Flower"};
    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_new_plant);

        spinner = findViewById(R.id.sp_typeOfPlant);

        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Array));
    }
}
