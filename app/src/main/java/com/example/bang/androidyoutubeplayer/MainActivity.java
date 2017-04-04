package com.example.bang.androidyoutubeplayer;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class MainActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final String YOUTUBE_API_KEY = "YOU ARE KEY";
    private static final int RECOVERY_REQUEST = 1;

    private YouTubePlayerView youTubeView;
    private MyPlayerStateChangeListener playerStateChangeListener;
    private MyPlaybackEventListener playbackEventListener;
    private YouTubePlayer player;

    private EditText editUrl;
    private EditText editStartTime;
    private EditText editEndTime;
    private TextView maxText;
    private TextView currentText;
    private TextView clientText;

    private long saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(YOUTUBE_API_KEY, this);

        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();

        editUrl = (EditText) findViewById(R.id.editUrl);
        editStartTime = (EditText) findViewById(R.id.editStartTime);
        editEndTime = (EditText) findViewById(R.id.editEndText);
        maxText = (TextView) findViewById(R.id.maxText);
        currentText = (TextView) findViewById(R.id.currentText);
        clientText = (TextView) findViewById(R.id.clientText);

        findViewById(R.id.load_bu).setOnClickListener(onClickListener);
        findViewById(R.id.start_bu).setOnClickListener(onClickListener);
        findViewById(R.id.pause_bu).setOnClickListener(onClickListener);
        findViewById(R.id.zero_bu).setOnClickListener(onClickListener);
        findViewById(R.id.back_bu).setOnClickListener(onClickListener);
        findViewById(R.id.goTO).setOnClickListener(onClickListener);
        findViewById(R.id.next_bu).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.load_bu) {
                player.loadVideo(editUrl.getText().toString().replace("https://youtu.be/",""));
            }
            if (v.getId() == R.id.start_bu) {
                player.play();
            }
            if (v.getId() == R.id.pause_bu) {
                player.pause();
            }
            if (v.getId() == R.id.zero_bu) {
                player.seekToMillis(0);
            }
            if (v.getId() == R.id.goTO) {
                if (editStartTime.getText().toString().length() > 0) {
                    player.seekToMillis(Integer.parseInt(editStartTime.getText().toString()));
                }
            }
            if (v.getId() == R.id.next_bu) {
                player.seekToMillis(player.getCurrentTimeMillis() + 100);
            }
            if (v.getId() == R.id.back_bu) {
                player.seekToMillis(player.getCurrentTimeMillis() - 100);
            }
        }
    };

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.player = player;
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(YOUTUBE_API_KEY, this);
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubeView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        @Override
        public void onPlaying() {
            // Called when playback starts, either due to user action or call to play().
            showMessage("Playing");

            maxText.setText(String.valueOf("MaxTime: "+player.getDurationMillis()));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int saveTiem = 0;
                    saveCurrentTime = System.currentTimeMillis();
                    try{
                        while(player.getCurrentTimeMillis() < player.getDurationMillis()) {
                            if (player.getCurrentTimeMillis() != saveTiem) {
                                saveTiem = player.getCurrentTimeMillis();
                                saveCurrentTime = System.currentTimeMillis();
                            }
                            if (editEndTime.getText().toString().compareToIgnoreCase("") != 0) {
                                if ((player.getCurrentTimeMillis() + (int) (System.currentTimeMillis() - saveCurrentTime)) / 100 == Integer.parseInt(editEndTime.getText().toString()) / 100) {
                                    player.pause();
                                }
                            }
                            mediaHandler.sendEmptyMessage(0);
                            if(player.isPlaying()){
                                mediaHandler.sendEmptyMessage(1);
                            }
                            Thread.sleep(10);
                        }
                    }catch (Exception e){
                        Log.e("Error","- "+e);
                    }
                }
            }).start();
        }

        @Override
        public void onPaused() {
            // Called when playback is paused, either due to user action or call to pause().
            showMessage("Paused");
        }

        @Override
        public void onStopped() {
            // Called when playback stops for a reason other than being paused.
            showMessage("Stopped");
        }

        @Override
        public void onBuffering(boolean b) {
            // Called when buffering starts or ends.
        }

        @Override
        public void onSeekTo(int i) {
            // Called when a jump in playback position occurs, either
            // due to user scrubbing or call to seekRelativeMillis() or seekToMillis()
        }
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoading() {
            // Called when the player is loading a video
            // At this point, it's not ready to accept commands affecting playback such as play() or pause()
        }

        @Override
        public void onLoaded(String s) {
            // Called when a video is done loading.
            // Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
            player.play();
        }

        @Override
        public void onAdStarted() {
            // Called when playback of an advertisement starts.
        }

        @Override
        public void onVideoStarted() {
            // Called when playback of the video starts.
        }

        @Override
        public void onVideoEnded() {
            // Called when the video reaches its end.
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            // Called when an error occurs.
        }
    }

    public Handler mediaHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    currentText.setText(String.valueOf("CurrentTime: " + player.getCurrentTimeMillis()));
                    break;
                case 1:
                    clientText.setText(String.valueOf("ClientTime: " + (player.getCurrentTimeMillis() + (int) (System.currentTimeMillis() - saveCurrentTime)) / 100 + "00"));
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}