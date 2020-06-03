package ru.vladimirvlasoff.videogallery;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityVideos extends AppCompatActivity {

    // список видео
    public ArrayList<ModelVideo> videosList = new ArrayList<>();
    // адаптер для RecyclerView
    private AdapterVideoList adapterVideoList;

    // главная функция активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        initializeViews();
        checkPermissions();
    }
    // инициализируем меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.videos_menu, menu);
        return true;
    }
    // обработчик нажатия на элементы меню
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // строка с информацией
        String aboutApp = "Курсовой проект по теме: \"Разработка приложения для просмотра видео\".\n" +
                "Выполнил студент второго курса Мининского университета, группы ПИМ-18 Власов Владимир.";
        // интент
        Intent intent = new Intent(ActivityVideos.this, ActivityAbout.class);
        // получаем id нажатого элемента
        switch (item.getItemId()) {
            case R.id.aboutApp:
                intent.putExtra("info", aboutApp);
                startActivity(intent);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return true;
    }
    // инициализируем компоненты
    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView_videos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapterVideoList = new AdapterVideoList(this, videosList);
        recyclerView.setAdapter(adapterVideoList);
    }
    // проверяем разрешения
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            } else {
                loadVideos();
            }
        } else {
            loadVideos();
        }
    }
    // делаем запрос на получение разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos();
            } else {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // загружаем видео
    private void loadVideos() {
        // в новом потоке
        new Thread() {
            @Override
            public void run() {
                super.run();
                // поля
                String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DURATION};
                // порядок сортировки
                String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";
                // курсор, для запросов
                Cursor cursor = getApplication().getContentResolver()
                        .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
                // если курсор существует
                if (cursor != null) {
                    // то получаем данные
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    // выполняем, пока не найдем все файлы
                    while (cursor.moveToNext()) {
                        // id
                        long id = cursor.getLong(idColumn);
                        // название
                        String title = cursor.getString(titleColumn);
                        // продолжительность
                        int duration = cursor.getInt(durationColumn);
                        // ссылка(путь к файлу)
                        Uri data = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                        // форматируем время
                        String duration_formatted;
                        int sec = (duration / 1000) % 60;
                        int min = (duration / (1000 * 60)) % 60;
                        int hrs = duration / (1000 * 60 * 60);

                        if (hrs == 0) {
                            duration_formatted = String.valueOf(min).concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                        } else {
                            duration_formatted = String.valueOf(hrs).concat(":".concat(String.format(Locale.UK, "%02d", min)
                                    .concat(":".concat(String.format(Locale.UK, "%02d", sec)))));
                        }
                        // добавляем в список
                        videosList.add(new ModelVideo(id, data, title, duration_formatted));
                        runOnUiThread(() -> adapterVideoList.notifyItemInserted(videosList.size() - 1));
                    }
                }

            }
        }.start();

    }
}
