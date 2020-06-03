package ru.vladimirvlasoff.videogallery;

import android.content.ContentUris;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    // кастомные компоненты
    private ProgressBar progressBar;
    private ImageView fullScreen;
    private DefaultTimeBar timeBar;
    // id
    long videoId;
    // флаг, для кнопки полноэкранного режима
    private boolean flag = false;
    // компоненты
    private PlayerView playerView;
    private SimpleExoPlayer player;

    private ActivityVideos videos = new ActivityVideos();

    private ArrayList<ModelVideo> vids = videos.videosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        hideActionBar();
        initializeViews();
        videoId = getIntent().getExtras().getLong("videoId");
    }
    // прячем ActionBar
    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
    }
    // инициализируем вью
    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progress_bar);
        fullScreen = findViewById(R.id.bt_fullscreen);
        timeBar = findViewById(R.id.exo_progress);

        LoadControl loadControl = new DefaultLoadControl();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(
                new AdaptiveTrackSelection.Factory(bandwidthMeter)
        );
    }
    // инииализируем плеер
    private void initializePlayer() {
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setKeepScreenOn(true);
        playerView.setPlayer(player);
        // обрабочик нажатий на кнопки в интерфейсе плеера
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
            }
            // при ошибке
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                player.stop();
            }
        });
        // обработчик для кнопки полноэкранного режима
        fullScreen.setOnClickListener(view -> {
            if (flag) {
                fullScreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen_black_24dp));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                flag = false;
            } else {
                fullScreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen_exit));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                flag = true;
            }
        });
        // через путь ставим видео в плеер
        Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId);
        MediaSource mediaSource = buildMediaSource(videoUri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }
    // создаем MediaSource(видео)
    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }
    // для выключения плеера
    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }
    // при запуске активности
    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }
    // при возобновлении активности
    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer();
        }
    }
    // при паузе
    @Override
    protected void onPause() {
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
        super.onPause();
    }
    // при остановке
    @Override
    protected void onStop() {
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
        super.onStop();
    }

}
