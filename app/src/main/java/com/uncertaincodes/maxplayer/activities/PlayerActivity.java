package com.uncertaincodes.maxplayer.activities;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.*;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.media3.common.*;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.source.ConcatenatingMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.TrackSelectionDialogBuilder;
import com.github.vkay94.dtpv.DoubleTapPlayerView;
import com.github.vkay94.dtpv.youtube.YouTubeOverlay;
import com.google.android.material.snackbar.Snackbar;
import com.tencent.mmkv.MMKV;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.models.VideoDetails;
import com.uncertaincodes.maxplayer.utils.Constants;
import com.uncertaincodes.maxplayer.utils.PreferenceUtils;
import com.uncertaincodes.maxplayer.utils.Utils;
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener, GestureDetector.OnGestureListener, AudioManager.OnAudioFocusChangeListener{

    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN
    }

    private boolean isLocked = false;
    private AudioManager audioManager = null;
    public PreferenceUtils preferenceUtils;
    private long playbackPosition;
    private int currentWindowIndex;
    private boolean playWhenReady = true;

    private RelativeLayout rl_guide;
    private ImageView imgGuide;
    private Button btnNext;
    private Button btnOk;
    private DoubleTapPlayerView playerView;
    private ExoPlayer player;
    private ArrayList<VideoDetails> playlist = new ArrayList<>();
    private int position;
    private String videoTitle;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private RelativeLayout root;
    private ImageView video_back, btnPip, unlock, exo_prev, exoPlayPause, exo_next, scaling, lock;
    private TextView video_title, exo_position, exo_duration;
    private ImageView btnAudio, btnCaption;
    private DefaultTimeBar exo_progress;
    private GestureDetectorCompat gestureDetectorCompat;
    private LinearLayout ll_message;
    private ImageView img_message;
    private TextView txt_message;
    private int brightness = 0;
    private int volume = 0;
    private float minSwipeX = 0f;
    private float minSwipeY = 0f;
    private long seekStart;
    private long seekChange;
    private long seekMax;
    private final long SEEK_STEP = 1000;

    private DefaultTrackSelector trackSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        preferenceUtils = new PreferenceUtils(this);
        setContentView(R.layout.activity_player);

        hideBottomBar();

        Bundle bundle = getIntent().getBundleExtra("VideoListBundle");
        if (bundle != null) {
            position = bundle.getInt("position");
            playlist.clear();
            playlist = bundle.getParcelableArrayList("videoArrayList");

            initializeViews();

            NextRenderersFactory renderersFactory = (NextRenderersFactory) new NextRenderersFactory(this)
                    .setEnableDecoderFallback(true)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);

            trackSelector = new DefaultTrackSelector(this);

            player = new ExoPlayer.Builder(this)
                    .setRenderersFactory(renderersFactory)
                    .setTrackSelector(trackSelector)
                    .build();

            applyListeners();

            playVideo();


            MMKV mmkv = MMKV.defaultMMKV();
            if (!mmkv.decodeBool(Constants.IS_GUIDE_SHOWED, false)) {
                showGuides();
            }

            playerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
                @Override
                public void onVisibilityChange(int visibility) {
                    if (visibility == View.VISIBLE) {
                        Utils.showSystemUi(playerView);
                    } else {
                        Utils.hideSystemUi(playerView);
                    }
                }
            });

            backPressed();
        }
    }

    private void applyListeners() {
        player.addListener(new Player.Listener() {
            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                float aspectRatio = (float) videoSize.width / videoSize.height;
                int rotationDegrees = videoSize.unappliedRotationDegrees;

                if (rotationDegrees == 90 || rotationDegrees == 270) {
                    // The video is rotated, so the aspect ratio should be inverted
                    aspectRatio = 1 / aspectRatio;
                }
                if (aspectRatio > 1) {
                    // Landscape video
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    // Portrait video
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    // Reset to default orientation when the video ends
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {

                playerView.setKeepScreenOn(isPlaying);
                if (player.isPlaying()) {
                    exoPlayPause.setImageResource(R.drawable.ic_controller_pause);
                } else {
                    exoPlayPause.setImageResource(R.drawable.ic_controller_play);
                }
            }
        });

    }


    private void showGuides() {
        MMKV mmkv = MMKV.defaultMMKV();

        rl_guide.setVisibility(View.VISIBLE);

        btnNext.setOnClickListener(v -> {
            imgGuide.setImageResource(R.drawable.img_guide2);
            btnOk.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
        });
        btnOk.setOnClickListener(v -> {
            mmkv.encode(Constants.IS_GUIDE_SHOWED, true);
//            playVideo();
            rl_guide.setVisibility(View.GONE);
        });
    }

    private void initializeViews() {
        rl_guide = findViewById(R.id.rl_guide);
        imgGuide = findViewById(R.id.imgGuide);
        btnOk = findViewById(R.id.btnOk);
        btnNext = findViewById(R.id.btnNext);

        playerView = findViewById(R.id.playerView);
        root = findViewById(R.id.root_layout);

        video_back = findViewById(R.id.video_back);
        btnPip = findViewById(R.id.btnPip);
        btnAudio = findViewById(R.id.btnAudio);
        btnCaption = findViewById(R.id.btnCaption);
        unlock = findViewById(R.id.unlock);
        lock = findViewById(R.id.lock);
        exo_prev = findViewById(R.id.exo_prev);
        exoPlayPause = findViewById(R.id.exoPlayPause);
        exo_next = findViewById(R.id.exo_next);
        scaling = findViewById(R.id.scaling);

        video_title = findViewById(R.id.video_title);
        exo_position = findViewById(R.id.exo_position);
        exo_duration = findViewById(R.id.exo_duration);

        exo_progress = findViewById(R.id.exo_progress);

        exo_progress.setBufferedColor(0x33FFFFFF);

        videoTitle = playlist.get(position).getDisplayName();
        video_title.setText(videoTitle);

        ll_message = findViewById(R.id.ll_message);
        img_message = findViewById(R.id.img_message);
        txt_message = findViewById(R.id.txt_message);

        exo_next.setOnClickListener(this);
        exo_prev.setOnClickListener(this);
        exoPlayPause.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        video_back.setOnClickListener(this);
        btnPip.setOnClickListener(this);
        btnAudio.setOnClickListener(this);
        btnCaption.setOnClickListener(this);

        scaling.setOnClickListener(fillListener);

        gestureDetectorCompat = new GestureDetectorCompat(this, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPicture = new PictureInPictureParams.Builder();
        }

        btnCaption.setOnLongClickListener(v -> {
            player.setTrackSelectionParameters(
                    player
                            .getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                            .build());
            Snackbar.make(v, "Caption Disabled.", 2500).show();
            return true;
        });
        btnAudio.setOnLongClickListener(v -> {
            player.setTrackSelectionParameters(
                    player
                            .getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                            .build());
            Snackbar.make(v, "Audio Disabled.", 2500).show();
            return true;
        });
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.exoPlayPause) {
            if (player == null)
                return;
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        }
        if (id == R.id.exo_next) {
            try {
                // Save playback state before releasing the player
                preferenceUtils.savePlaybackState(player, playlist.get(position).getPath());
                player.stop();
                position++;

                playVideo();
                video_title.setText(playlist.get(position).getDisplayName());
            } catch (Exception e) {
                Toast.makeText(this, "No Next Video", Toast.LENGTH_SHORT).show();
                position--;
                finish();
            }
        }
        if (id == R.id.exo_prev) {
            try {
                // Save playback state before releasing the player
                preferenceUtils.savePlaybackState(player, playlist.get(position).getPath());
                player.stop();
                position--;
                playVideo();
                video_title.setText(playlist.get(position).getDisplayName());
            } catch (Exception e) {
                Toast.makeText(this, "No Previous Video", Toast.LENGTH_SHORT).show();
                position++;
                finish();
            }
        }
        if (id == R.id.lock) {
            isLocked = false;
            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            lock.setVisibility(View.INVISIBLE);
        }
        if (id == R.id.unlock) {
            isLocked = true;
            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.INVISIBLE);
            lock.setVisibility(View.VISIBLE);
        }
        if (id == R.id.video_back) {
            if (player != null) {
                player.release();
            }
            finish();
        }
        if (id == R.id.btnPip) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                    final Format format = player.getVideoFormat();

                    if (format != null) {
                        // https://github.com/google/ExoPlayer/issues/8611
                        // TODO: Test/disable on Android 11+
                        final View videoSurfaceView = playerView.getVideoSurfaceView();
                        if (videoSurfaceView instanceof SurfaceView) {
                            ((SurfaceView) videoSurfaceView).getHolder().setFixedSize(format.width, format.height);
                        }

                        Rational rational = Utils.getRational(format);
                        if (rational.floatValue() > rationalLimitWide.floatValue())
                            rational = rationalLimitWide;
                        else if (rational.floatValue() < rationalLimitTall.floatValue())
                            rational = rationalLimitTall;
                        pictureInPicture.setAspectRatio(rational);
                    }
                    enterPictureInPictureMode(pictureInPicture.build());
                } else {
                    Toast.makeText(this, "Device does not support Picture-in-Picture mode", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Device does not support Picture-in-Picture mode", Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.btnAudio) {

            TrackSelectionDialogBuilder dialogBuilder = new TrackSelectionDialogBuilder(
                    this,
                    "Select Audio",
                    player,
                    C.TRACK_TYPE_AUDIO);

            dialogBuilder.build().show();

            Snackbar.make(view, "Long press to disable audio.", 2500).show();
        }
        if (id == R.id.btnCaption) {

            TrackSelectionDialogBuilder dialogBuilder = new TrackSelectionDialogBuilder(
                    this,
                    "Select Subtitle",
                    player,
                    C.TRACK_TYPE_TEXT);

            dialogBuilder.build().show();

            Snackbar.make(view, "Long press to disable audio.", 2500).show();
        }
    }

    final Rational rationalLimitWide = new Rational(239, 100);
    final Rational rationalLimitTall = new Rational(100, 239);

    PictureInPictureParams.Builder pictureInPicture;
    boolean isCrossChecked;

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        isCrossChecked = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
            playerView.hideController();
        } else {
            playerView.showController();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isCrossChecked) {
            player.release();
            finish();
        }
    }

    View.OnClickListener fillListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_full_screen);

            scaling.setOnClickListener(zoomListener);
        }
    };
    View.OnClickListener zoomListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_zoom);

            scaling.setOnClickListener(fitListener);
        }
    };
    View.OnClickListener fitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_fit);

            scaling.setOnClickListener(fillListener);
        }
    };


    private void playVideo() {

        String path = playlist.get(position).getPath();
        Uri uri = Uri.parse(path);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));

        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < playlist.size(); i++) {
            new File(String.valueOf(playlist.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }

        doubleTapEnable();
        playerView.setKeepScreenOn(true);

        playbackPosition = preferenceUtils.getPlayBackPosition(playlist.get(position).getPath());
        currentWindowIndex = preferenceUtils.getCurrentWindow(playlist.get(position).getPath());
        playWhenReady = preferenceUtils.isPlayWhenReady(playlist.get(position).getPath());

        player.prepare(concatenatingMediaSource);
        player.seekTo(currentWindowIndex, playbackPosition);
        player.setPlayWhenReady(playWhenReady);

        playError();
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                Toast.makeText(PlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
                Log.d("playError", error.getMessage());
            }
        });
        player.setPlayWhenReady(true);
    }

    private void releasePlayer() {
        if (player != null) {
            // Save playback state before releasing the player
            preferenceUtils.savePlaybackState(player, playlist.get(position).getPath());
            player.release();
            player = null;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInPictureInPictureMode()) {
                player.setPlayWhenReady(true);
            } else {
                player.setPlayWhenReady(false);
                player.getPlaybackState();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }

        if (brightness != 0) {
            setBrightness(brightness);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void backPressed() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void doubleTapEnable() {
        playerView.setPlayer(player);
        YouTubeOverlay youTubeOverlay = findViewById(R.id.youtube_overlay);
        youTubeOverlay.performListener(new YouTubeOverlay.PerformListener() {

            @org.jetbrains.annotations.Nullable
            @Override
            public Boolean shouldForward(@NotNull Player player, @NotNull DoubleTapPlayerView playerView, float posX) {
                if (player.getPlaybackState() == PlaybackState.STATE_ERROR ||
                        player.getPlaybackState() == PlaybackState.STATE_NONE ||
                        player.getPlaybackState() == PlaybackState.STATE_STOPPED) {

                    playerView.cancelInDoubleTapMode();
                    return null;
                }

                if (player.getCurrentPosition() > 500 && posX < playerView.getWidth() * 0.35)
                    return false;

                if (player.getCurrentPosition() < player.getDuration() && posX > playerView.getWidth() * 0.65)
                    return true;

                return null;
            }

            @Override
            public void onAnimationStart() {
                youTubeOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {
                youTubeOverlay.setVisibility(View.GONE);
            }
        });
        youTubeOverlay.player(player);
        playerView.setOnTouchListener((view, motionEvent) -> {

            playerView.setDoubleTapEnabled(false);
            if (!isLocked) {
                playerView.setDoubleTapEnabled(true);
                gestureDetectorCompat.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    ll_message.setVisibility(View.GONE);

                }
            }

            return false;
        });
    }

    @Override
    public void onAudioFocusChange(int i) {
        if (i <= 0) player.pause();
    }

    @Override
    public boolean onDown(@NonNull MotionEvent motionEvent) {
        minSwipeY = 0;
        minSwipeX = 0;
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float distanceX, float distanceY) {

        int sWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int sHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        int border = (int) (25 * Resources.getSystem().getDisplayMetrics().density);
        assert motionEvent != null;
        if (motionEvent.getX() < border || motionEvent.getY() < border || motionEvent.getX() > sWidth - border || motionEvent.getY() > sHeight - border)
            return false;

        if (minSwipeY == 0 || minSwipeX == 0) {
            minSwipeY = 0.0001f;
            minSwipeX = 0.0001f;
            return false;
        }
        //minSwipeY for slowly increasing brightness & volume on swipe --> try changing 50 (<50 --> quick swipe & > 50 --> slow swipe
        // & test with your custom values
        minSwipeY += distanceY;
        minSwipeX += distanceX;
        if (abs(distanceX) < abs(distanceY) && abs(minSwipeY) > 50) {
            if (motionEvent.getX() < (float) sWidth / 2) {
                // brightness
                ll_message.setVisibility(View.VISIBLE);

                boolean increase = distanceY > 0;
                int newValue = increase ? brightness + 1 : brightness - 1;
                if (newValue >= 0 && newValue <= 30) {
                    brightness = newValue;
                }

                setMessage(R.drawable.ic_brightness_medium, String.valueOf(brightness));
                setBrightness(brightness);
            } else {
                // volume
                ll_message.setVisibility(View.VISIBLE);

                if (audioManager != null) {

                    volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    boolean increase = distanceY > 0;
                    int newValue = increase ? volume + 1 : volume - 1;

                    if (newValue >= 0 && newValue <= maxVolume) {
                        volume = newValue;
                    }

                    setMessage(R.drawable.ic_volume, String.valueOf(volume));
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                }
            }
            minSwipeY = 0f;
        } else if (Math.abs(distanceX) > Math.abs(distanceY) && abs(minSwipeX) > 50) {
            // Horizontal swipe
            seekStart = player.getCurrentPosition();
            seekChange = 0L;
            seekMax = player.getDuration();
            long position = 0;
            float distanceDiff = Math.max(0.5f, Math.min(Math.abs(Utils.pxToDp(PlayerActivity.this, distanceX) / 4), 10.f));

            if (distanceX > 0) {
                if (seekStart + seekChange - SEEK_STEP * distanceDiff >= 0) {
                    player.setSeekParameters(SeekParameters.PREVIOUS_SYNC);
                    seekChange -= SEEK_STEP * distanceDiff;
                    position = seekStart + seekChange;
                    player.seekTo(position);
                }
            } else {
                player.setSeekParameters(SeekParameters.NEXT_SYNC);
                if (seekMax == C.TIME_UNSET) {
                    seekChange += SEEK_STEP * distanceDiff;
                    position = seekStart + seekChange;
                    player.seekTo(position);
                } else if (seekStart + seekChange + SEEK_STEP < seekMax) {
                    seekChange += SEEK_STEP * distanceDiff;
                    position = seekStart + seekChange;
                    player.seekTo(position);
                }
            }
            minSwipeX = 0f;
        }

        return true;
    }

    private void setMessage(int drawable, String message) {
        img_message.setImageResource(drawable);
        txt_message.setText(message);
    }

    private void setBrightness(int value) {
        float d = 1.0f / 30;
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.screenBrightness = d * value;
        this.getWindow().setAttributes(lp);
    }

    private void setFullScreen() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void hideBottomBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decodeView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decodeView.setSystemUiVisibility(uiOptions);
        }
    }
}