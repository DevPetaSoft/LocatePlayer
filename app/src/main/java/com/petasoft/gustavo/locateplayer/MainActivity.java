package com.petasoft.gustavo.locateplayer;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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
    private static Handler handler = new Handler();

    private int duration;


    private final Runnable runnable = new Runnable() {
        public void run() {
            if(music!=null) {
                if (music.isPlaying()) {
                    duration = music.getCurrentPosition();
                    int minutes = (int) ((duration / (1000 * 60)) % 60);
                    int seconds = (int) (duration / (1000) % 60);
                    mSeekBar.setProgress(duration);
                    mSeekBar.setMax(music.getDuration());
                    currentPositionText.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final File file;
        final ListView listView = (ListView) findViewById(R.id.musicListView);
        currentPositionText = (TextView) findViewById(R.id.currentTime);
        durationText = (TextView) findViewById(R.id.musicTime);
        final ImageView playPauseButton = (ImageView) findViewById(R.id.imageView);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        music = new MediaPlayer();
        music.setOnCompletionListener(onCompletion);
        mSeekBar.setOnSeekBarChangeListener(seekBarChanged);
        minhaLista = new ArrayList<String>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


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
                    setMusic(Environment.getExternalStorageDirectory().toString() + "/Music/" + selectedFromList, position);
                    music.start();
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

        handler.postDelayed(runnable, 1000);

    }

    private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            nextMusic();
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setMusic(String uri, int listIndex) throws IOException {
        final TextView artistTextView = (TextView) findViewById(R.id.artistText);
        final TextView musicNameText = (TextView) findViewById(R.id.musicNameText);
        final ImageView albumImage = (ImageView) findViewById(R.id.albumImage);

        mSeekBar.setProgress(0);
        music.setDataSource(uri);
        music.prepare();
        mmr.setDataSource(uri);
        mSeekBar.setMax(music.getDuration());
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


    public void nextMusic(){
        musicListIndex += 1;
        musicListIndex = musicListIndex % minhaLista.size();
        music.reset();
        try {
            setMusic(Environment.getExternalStorageDirectory() + "/Music/" + minhaLista.get(musicListIndex),musicListIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        music.start();
        play = true;
        ImageView playPauseButton = (ImageView) findViewById(R.id.imageView);
        playPauseButton.setImageResource(R.drawable.pausebutton);
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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        if(music != null) {
            music.stop();
            music.reset();
            music.release();
        }
        music = null;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
