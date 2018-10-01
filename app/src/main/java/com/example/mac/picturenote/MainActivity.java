package com.example.mac.picturenote;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    Bitmap selectedImage;
    ImageView imageView;

    static ArrayList<Bitmap> PictureAd;
    //menuinflater ile menuyu çıkartmayı mainacitiviye tanımladık.



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_picture, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_picture){

            Intent intent = new Intent(getApplicationContext(), PictureAdd.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        }

        if(item.getItemId() == R.id.take_picture){

            Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            startActivityForResult(i,5);

        }

        if(item.getItemId() == R.id.app_bar_search){

        }
        return super.onOptionsItemSelected(item);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 5){

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_camera);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            Intent intent = new Intent(MainActivity.this, PictureAdd.class);
            intent.putExtra("new", byteArray);
            startActivity(intent);

        } else if (requestCode == RESULT_CANCELED){

            Toast.makeText(getApplicationContext(),
                    "Kamera Desteklemiyor yada ilem iptal",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView  listView= (ListView) findViewById(R.id.listView);

        final ArrayList<String> pictureName = new ArrayList<String>();
        final ArrayList<String> pictureNote = new ArrayList<String>();
        PictureAd = new ArrayList<Bitmap>();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pictureName);

        listView.setAdapter(arrayAdapter);

        try {


        PictureAdd.database = this.openOrCreateDatabase("Picture",  MODE_PRIVATE, null);
        PictureAdd.database.execSQL("CREATE TABLE IF NOT EXISTS picture (name VARCHAR, image BLOB, text VARCHAR) ");

        Cursor cursor = PictureAdd.database.rawQuery("SELECT  * FROM picture", null);

        int nameIx = cursor.getColumnIndex("name");
        int imageIX = cursor.getColumnIndex("image");
        int textIx = cursor.getColumnIndex("text");
        cursor.moveToFirst();

        while (cursor != null) {

                pictureName.add(cursor.getString(nameIx));
                pictureNote.add(cursor.getString(textIx));


            byte[] byteArray = cursor.getBlob(imageIX);

            Bitmap image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            PictureAd.add(image);
            cursor.moveToNext();

            arrayAdapter.notifyDataSetChanged();


            }

        }catch (Exception e){

            e.printStackTrace();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                Intent intent = new Intent(getApplicationContext(), PictureAdd.class);
                intent.putExtra("info", "old");
                intent.putExtra("name", PictureAd.get(i));
                intent.putExtra("i", i );
                intent.putExtra("name", pictureName.get(i));
                intent.putExtra("text", pictureNote.get(i));

                startActivity(intent);
            }
        });


    }



}
