package com.example.mac.picturenote;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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


        String info = intent.getStringExtra("info");


        //actionbarı off yapar.
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (info.equalsIgnoreCase("new") ) {

            Bundle extras = getIntent().getExtras();
            byte[] byteArray = extras.getByteArray("picture");

            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray,0, byteArray.length);
            imageView.setImageBitmap(bmp);
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
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void save (View view){


        String pictureName =editText.getText().toString();
        String pictureNote = editText2.getText().toString();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        selectedImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        if(editText.getText().toString().trim().length() == 1 && editText2.getText().toString().trim().length() ==1 ) {
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

            }    catch (Exception e) {

                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        }else {

            Toast.makeText(PictureAdd.this, "Lütfen Boşlukları Doldurunuz", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){

            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}
