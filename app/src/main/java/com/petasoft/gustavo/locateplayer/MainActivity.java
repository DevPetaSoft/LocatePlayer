package com.petasoft.gustavo.locateplayer;

import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final File file;

        final ListView listView = (ListView) findViewById(R.id.musicListView);
        final TextView artistTextView = (TextView) findViewById(R.id.artistText);

        final ImageView playPauseButton= (ImageView) findViewById(R.id.imageView);
        minhaLista = new ArrayList<String>();
        try {

            String root_sd = Environment.getExternalStorageDirectory().toString()+"/Music";
            file = new File(root_sd );
            File list[] = file.listFiles();

            for (int i = 0; i < list.length; i++) {
                minhaLista.add(list[i].getName());
            }
            music.setDataSource(Environment.getExternalStorageDirectory() + "/Music/"+minhaLista.get(0));
            mmr.setDataSource(Environment.getExternalStorageDirectory() + "/Music/" + minhaLista.get(0));
            String artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            artistTextView.setText(artistName);
            musicListIndex = 0;
            music.prepare();
            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, minhaLista));
        } catch (Exception ex)
        {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
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
            public void onItemClick(AdapterView<?> parent, View view,int position, long id)
            {

                String selectedFromList = (listView.getItemAtPosition(position).toString());
                try {
                    music.reset();
                    music.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Music/" + selectedFromList);
                    music.prepare();
                    music.start();
                    play = true;
                    playPauseButton.setImageResource(R.drawable.pausebutton);
                }
                catch (Exception ex)
                {
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

    }

    public void playPauseMusic(View view){
        ImageView imgView= (ImageView) findViewById(R.id.imageView);
        if(play){
            imgView.setImageResource(R.drawable.playbutton);
            music.pause();
        }
        else{
            imgView.setImageResource(R.drawable.pausebutton);
            music.start();
        }
        play = !play;
    }

    public void nextMusic(View view) throws IOException {
        musicListIndex += 1;
        musicListIndex = musicListIndex%minhaLista.size();
        music.reset();
        music.setDataSource(Environment.getExternalStorageDirectory() + "/Music/"+minhaLista.get(musicListIndex));
        mmr.setDataSource(Environment.getExternalStorageDirectory() + "/Music/" + minhaLista.get(musicListIndex));
        String artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        final TextView artistTextView = (TextView) findViewById(R.id.artistText);
        artistTextView.setText(artistName);
        music.prepare();
        music.start();
        play = true;
        ImageView playPauseButton= (ImageView) findViewById(R.id.imageView);
        playPauseButton.setImageResource(R.drawable.pausebutton);
    }

    public void previousMusic(View view) throws IOException {
        musicListIndex -= 1;
        if(musicListIndex<0){
            musicListIndex = minhaLista.size()-1;
        }
        music.reset();
        music.setDataSource(Environment.getExternalStorageDirectory() + "/Music/"+minhaLista.get(musicListIndex));
        mmr.setDataSource(Environment.getExternalStorageDirectory() + "/Music/" + minhaLista.get(musicListIndex));
        String artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        final TextView artistTextView = (TextView) findViewById(R.id.artistText);
        artistTextView.setText(artistName);
        music.prepare();
        music.start();
        play = true;
        ImageView playPauseButton= (ImageView) findViewById(R.id.imageView);
        playPauseButton.setImageResource(R.drawable.pausebutton);
    }
}
