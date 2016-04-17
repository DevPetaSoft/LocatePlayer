package com.petasoft.gustavo.locateplayer;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static boolean play = false;
    private static MediaPlayer music = new MediaPlayer();
    private static int musicListIndex;
    private static List<String> minhaLista;
    private static MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    private byte[] art;
    private boolean isMoveingSeekBar = false;
    private SeekBar mSeekBar = null;
    private static TextView durationText;
    private static TextView currentPositionText;

    private int duration;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final File file;
        final ListView listView = (ListView) findViewById(R.id.musicListView);
        currentPositionText = (TextView) findViewById(R.id.currentTime);
        durationText = (TextView) findViewById(R.id.musicTime);
        final ImageView playPauseButton = (ImageView) findViewById(R.id.imageView);
        final Handler handler = new Handler();
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        mSeekBar.setOnSeekBarChangeListener(seekBarChanged);
        mSeekBar.setProgress(0);


        minhaLista = new ArrayList<String>();
        //Carregamento da lista de musicas
        try {

            String root_sd = Environment.getExternalStorageDirectory().toString() + "/Music";
            file = new File(root_sd);
            File list[] = file.listFiles();

            for (int i = 0; i < list.length; i++) {
                minhaLista.add(list[i].getName());
            }
            setMusic(Environment.getExternalStorageDirectory() + "/Music/" + minhaLista.get(0),0);
            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, minhaLista));
        } catch (Exception ex) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage(ex.getMessage());
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        //Evento de click de um elemento de um list view

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedFromList = (listView.getItemAtPosition(position).toString());
                try {
                    music.reset();
                    music.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Music/" + selectedFromList);
                    music.prepare();
                    music.start();
                    mSeekBar.setMax(music.getDuration());
                    play = true;
                    playPauseButton.setImageResource(R.drawable.pausebutton);
                } catch (Exception ex) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage(ex.getMessage());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        final Runnable r = new Runnable() {
            public void run() {

                if(music.isPlaying()) {
                    duration = music.getCurrentPosition();
                    int minutes = (int)((duration/(1000*60))%60);
                    int seconds = (int)(duration/(1000)%60);
                    mSeekBar.setProgress(duration);
                    mSeekBar.setMax(music.getDuration());
                    currentPositionText.setText(String.format("%02d:%02d",minutes,seconds));
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(r, 1000);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setMusic(String uri, int listIndex) throws IOException {
        final TextView artistTextView = (TextView) findViewById(R.id.artistText);
        final TextView musicNameText = (TextView) findViewById(R.id.musicNameText);
        final ImageView albumImage = (ImageView) findViewById(R.id.albumImage);

       // mSeekBar.setProgress(0);

        mSeekBar.setMax(music.getDuration());
        music.setDataSource(uri);
        music.prepare();
        mmr.setDataSource(uri);
        String artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String musicName = minhaLista.get(listIndex).replace(".mp3", "");
        int duration = music.getDuration();
        int minutes = (int)((duration/(1000*60))%60);
        int seconds = (int)(duration/(1000)%60);;
        durationText.setText(String.format("%02d:%02d",minutes,seconds));
        art = mmr.getEmbeddedPicture();
        if (art != null) {
            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
            albumImage.setImageBitmap(songImage);
        }
        else{
            albumImage.setImageDrawable(getDrawable(R.drawable.defaultalbumimage));
        }
        musicNameText.setText(musicName);
        artistTextView.setText(artistName);
        musicListIndex = listIndex;
    }

    public void playPauseMusic(View view) {
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        if (play) {
            imgView.setImageResource(R.drawable.playbutton);
            music.pause();
        } else {
            imgView.setImageResource(R.drawable.pausebutton);
            music.start();
        }
        play = !play;
    }

    public void nextMusic(View view) throws IOException {
        musicListIndex += 1;
        musicListIndex = musicListIndex % minhaLista.size();
        music.reset();
        setMusic(Environment.getExternalStorageDirectory() + "/Music/" + minhaLista.get(musicListIndex),musicListIndex);
        music.start();
        play = true;
        ImageView playPauseButton = (ImageView) findViewById(R.id.imageView);
        playPauseButton.setImageResource(R.drawable.pausebutton);
    }

    public void previousMusic(View view) throws IOException {
        musicListIndex -= 1;
        if (musicListIndex < 0) {
            musicListIndex = minhaLista.size() - 1;
        }
        music.reset();
        setMusic(Environment.getExternalStorageDirectory() + "/Music/" + minhaLista.get(musicListIndex),musicListIndex);
        music.start();
        play = true;
        ImageView playPauseButton = (ImageView) findViewById(R.id.imageView);
        playPauseButton.setImageResource(R.drawable.pausebutton);
    }


    private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isMoveingSeekBar = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isMoveingSeekBar = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isMoveingSeekBar) {
                music.seekTo(progress);
                int minutes = (int)((progress/(1000*60))%60);
                int seconds = (int)(progress/(1000)%60);
                currentPositionText.setText(String.format("%02d:%02d",minutes,seconds));
                Log.i("OnSeekBarChangeListener", "onProgressChanged");
            }
        }
    };


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.petasoft.gustavo.locateplayer/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.petasoft.gustavo.locateplayer/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


}
