package ru.vladimirvlasoff.videogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ActivityAbout extends AppCompatActivity {

    // текствью
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        // находим по id
        textView = findViewById(R.id.text_view_info);
        // получаем интент
        Intent intent = getIntent();
        // получаем строку
        String aboutApp = intent.getStringExtra("info");
        // устанавливаем текст
        textView.setText(aboutApp);
    }
}
