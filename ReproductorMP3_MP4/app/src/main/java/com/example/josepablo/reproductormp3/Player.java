package com.example.josepablo.reproductormp3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class Player extends AppCompatActivity {
    static MediaPlayer mp;
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    byte[] artbyte;

    ArrayList<File> mySongs;
    int position;
    Uri u, custom;
    Thread updateSeekBar;
    Handler mHandler = new Handler();

    SeekBar sb, volume;
    Button btPlay, btFF, btFB, btNxt, btPv;
    ImageView art;
    TextView album, artist, genre, length;
    Switch loopSwitch;
    AudioManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btPlay = (Button) findViewById(R.id.btPlay);
        btFF = (Button) findViewById(R.id.btFF);
        btFB = (Button) findViewById(R.id.btFB);
        btNxt = (Button) findViewById(R.id.btNxt);
        btPv = (Button) findViewById(R.id.btPv);
        art = (ImageView) findViewById(R.id.art);
        album = (TextView) findViewById(R.id.album);
        artist = (TextView) findViewById(R.id.artist);
        genre = (TextView) findViewById(R.id.genre);
        length = (TextView) findViewById(R.id.length);
        loopSwitch = (Switch) findViewById(R.id.loopSwitch);

        volume = (SeekBar) findViewById(R.id.volumeBar);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxV = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curV = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume.setMax(maxV);
        volume.setProgress(curV);
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seeked_progess;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb = (SeekBar) findViewById(R.id.seekBar);
        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mp.getDuration();
                int currentPosition = 0;
                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mp.getCurrentPosition();
                        sb.setProgress(currentPosition);
                        currentPosition = 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (mp != null) {
            try {
                mp.reset();
                mp.prepare();
                mp.stop();
                mp.release();
                mp = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent i = getIntent();
        Bundle b = i.getExtras();

        mySongs = (ArrayList) b.getParcelableArrayList("songlist");
        position = b.getInt("pos", 0);

        u = Uri.parse(mySongs.get(position).toString());

        mp = MediaPlayer.create(getApplicationContext(), u);
        sb.setProgress(0);// To set initial progress, i.e zero in starting of the song
        sb.setMax(mp.getDuration());// To set the max progress, i.e duration of the song
        mp.start();
        updateSeekBar.start();


        mmr.setDataSource(u.toString());
        try {
            artbyte = mmr.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(artbyte, 0, artbyte.length);
            art.setImageBitmap(songImage);
        } catch (Exception e) {
            art.setBackgroundColor(Color.GRAY);
        }

        try {
            int duration = 0;
            String dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (dur != null) {
                duration = Integer.parseInt(dur);
            }
            duration = duration / 1000;
            long h = duration / 3600;
            long m = (duration - h * 3600) / 60;
            long s = duration - (h * 3600 + m * 60);
            if (h == 0 || m == 0) {
                length.setText(m + ":" + s);
            }
            //length.setText(h + ":" + m + ":" + s);
        } catch (Exception e) {
            length.setText("Unknown Length");
        }

        try {
            genre.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
            genre.setText("Unknown Genre");
        }

        try {
            artist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        } catch (Exception e) {
            artist.setText("Unknown Artist");
        }

        try {
            album.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        } catch (Exception e) {
            album.setText("Unknown Album");
        }


        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seeked_progess;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seeked_progess = progress;
                if (fromUser) {
                    Runnable mRunnable = new Runnable() {

                        @Override
                        public void run() {
                            int min, sec;

                            if (mp != null /*Checking if the
                       music player is null or not otherwise it
                       may throw an exception*/) {
                                int mCurrentPosition = sb.getProgress();

                                min = mCurrentPosition / 60;
                                sec = mCurrentPosition % 60;

                                Log.e("Music Player Activity", "Minutes : " + min + " Seconds : " + sec);
                            }
                            mHandler.postDelayed(this, 1000);
                        }
                    };
                    mRunnable.run();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });

        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mp.isPlaying()) {
                    btPlay.setBackgroundResource(R.drawable.ic_play);
                    mp.pause();
                } else {
                    btPlay.setBackgroundResource(R.drawable.ic_pausa);
                    mp.start();
                }
            }
        });

        btFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.seekTo(mp.getCurrentPosition() + 5000);
            }
        });

        btFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.seekTo(mp.getCurrentPosition() - 5000);
            }
        });

        btNxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mp.reset();
                    mp.prepare();
                    mp.stop();
                    mp.release();
                    mp = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                position = (position + 1) % mySongs.size();
                u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                getSongInfo();
                btPlay.setBackgroundResource(R.drawable.ic_pausa);
                sb.setProgress(0);// To set initial progress, i.e zero in starting of the song
                sb.setMax(mp.getDuration());// To set the max progress, i.e duration of the song
                mp.start();
            }
        });

        btPv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mp.reset();
                    mp.prepare();
                    mp.stop();
                    mp.release();
                    mp = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                position = (position - 1 < 0) ? mySongs.size() - 1 : position - 1;
                u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                getSongInfo();
                btPlay.setBackgroundResource(R.drawable.ic_pausa);
                mp.start();
                sb.setProgress(0);// To set initial progress, i.e zero in starting of the song
                sb.setMax(mp.getDuration());// To set the max progress, i.e duration of the song
            }
        });

        loopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (loopSwitch.isChecked()) {
                    mp.setLooping(true);
                    if (mp.getCurrentPosition() == mp.getDuration()) {
                        mp.start();
                    }
                } else {
                    mp.setLooping(false);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            int index = volume.getProgress();
            volume.setProgress(index + 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            int index = volume.getProgress();
            volume.setProgress(index - 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void getSongInfo() {
        mmr.setDataSource(u.toString());
        try {
            artbyte = mmr.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(artbyte, 0, artbyte.length);
            art.setImageBitmap(songImage);
            art.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) {
            art.setImageBitmap(null);
            art.setBackgroundColor(Color.GRAY);
        }

        try {
            int duration = 0;
            String dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (dur != null) {
                duration = Integer.parseInt(dur);
            }
            duration = duration / 1000;
            long h = duration / 3600;
            long m = (duration - h * 3600) / 60;
            long s = duration - (h * 3600 + m * 60);
            if (h == 0 || m == 0) {
                length.setText(m + ":" + s);
            }
            //length.setText(h + ":" + m + ":" + s);
        } catch (Exception e) {
            length.setText("Unknown Length");
        }

        try {
            genre.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
            genre.setText("Unknown Genre");
        }

        try {
            artist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        } catch (Exception e) {
            artist.setText("Unknown Artist");
        }

        try {
            album.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        } catch (Exception e) {
            album.setText("Unknown Album");
        }
    }
}
