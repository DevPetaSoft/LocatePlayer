package com.petasoft.gustavo.locateplayer;

import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final File file;
        final List<String> minhaLista;

        final ListView listView = (ListView) findViewById(R.id.musicListView);

        minhaLista = new ArrayList<String>();
        try {

            String root_sd = Environment.getExternalStorageDirectory().toString()+"/Music";
            file = new File(root_sd );
            File list[] = file.listFiles();

            for (int i = 0; i < list.length; i++) {
                minhaLista.add(list[i].getName());
            }
            //musica.setDataSource(Environment.getExternalStorageDirectory() + "/Music/"+minhaLista.get(0));
            //musica.prepare();
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

    }
}
