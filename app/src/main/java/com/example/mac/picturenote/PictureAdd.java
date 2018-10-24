package com.example.mac.picturenote;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Objects;

public class PictureAdd extends AppCompatActivity {

    ImageView imageView;
    EditText editText;
    EditText editText2;
    Bitmap selectedImage;
    static SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_add);

        imageView = (ImageView)  findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);


        Button  button= (Button) findViewById(R.id.button);
        Intent intent = getIntent();
        initBackroundImage();
        String info = intent.getStringExtra("info");

        //actionbarı off yapar.
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //listviewda kaydedilen veriyi pictureadda  açar. eğer yeniyse.
        if (info.equalsIgnoreCase("new") ) {

            Bitmap background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.background);
            imageView.setImageBitmap(background);
            button.setVisibility(View.VISIBLE);

        }else {

            String name = intent.getStringExtra("name");
            String text = intent.getStringExtra("text");
            editText.setText(name);
            editText2.setText(text);
            int i  = intent.getIntExtra("i", 0);

            imageView.setImageBitmap(MainActivity.PictureAd.get(i));
            imageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);

        }

  }

         public void select (View view){
        //izin verilmedi ise bu kod yazılır. kullanıcdan izin isetemek için.

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        } else
        //izin varsa bu kod çalışır.
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //bir sonuç için intent yapılıyor.

            startActivityForResult(intent, 1);
        }

        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode ==2 ){

            if(grantResults.length  >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //bir sonuç için intent yapılıyor.

                startActivityForResult(intent, 1);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode ==1 && resultCode== RESULT_OK && data != null){

            Uri image = data.getData();
            try {
                selectedImage= MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);

                imageView.setImageBitmap(selectedImage);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode == 5){


            Bitmap  imagek = (Bitmap) data.getExtras().get("data");


            imageView.setImageBitmap(imagek);



        } else if (requestCode == RESULT_CANCELED){

            Toast.makeText(getApplicationContext(),
                    "Kamera Desteklemiyor yada ilem iptal",
                    Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void save (View view){

        String pictureName =editText.getText().toString();
        String pictureNote = editText2.getText().toString();


        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();

        Bitmap bitmap = imageView.getDrawingCache();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);



        byte[] byteArray = outputStream.toByteArray();


        if(editText.getText().toString().trim().length() == 0 || editText2.getText().toString().trim().length() ==0  ) {

            Toast.makeText(PictureAdd.this, "Lütfen Boşlukları Doldurunuz", Toast.LENGTH_SHORT).show();

        } else

        {
            try
            {
                database = this.openOrCreateDatabase("Picture", MODE_PRIVATE, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS picture (name VARCHAR, image BLOB, text VARCHAR)");


                String sqlString = "INSERT INTO picture (name, image, text) VALUES (?,?,?)";
                SQLiteStatement statement = database.compileStatement(sqlString);
                statement.bindString(1, pictureName);
                statement.bindBlob(2, byteArray);
                statement.bindString(3, pictureNote);
                statement.execute();

            }  catch (Exception e) {

                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        }
     }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){

            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }

        if (item.getItemId() == R.id.take_picture){

            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(i, 5);

        }
        return super.onOptionsItemSelected(item);

    }
     //menü ekleme

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_camera,menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void initBackroundImage(){

        ImageView background = findViewById(R.id.imageView);

        Glide.with(getApplicationContext()).load(CAMERA_SERVICE).into(imageView);

        Glide.with(this)
                .load(R.drawable.background)
                .into(background);
    }
}
