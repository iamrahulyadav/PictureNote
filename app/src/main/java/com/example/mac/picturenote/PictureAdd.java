package com.example.mac.picturenote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PictureAdd extends AppCompatActivity {

    ImageView mImageView;
    EditText editText;
    EditText editText2;
    Bitmap selectedImage;
    static SQLiteDatabase database;

    //SEMIH
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;

    /////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_add);

        mImageView = (ImageView)  findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);


        Button  button= (Button) findViewById(R.id.button);
        Intent intent = getIntent();

        initBackroundImage();
        isStoragePermissionGranted();

        String info = intent.getStringExtra("info");

        //actionbarı off yapar.
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //listviewda kaydedilen veriyi pictureadda  açar. eğer yeniyse.
        if (info.equalsIgnoreCase("new") ) {

            Bitmap background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.background);
            mImageView.setImageBitmap(background);
            button.setVisibility(View.VISIBLE);

        }else {

            String name = intent.getStringExtra("name");
            String text = intent.getStringExtra("text");
            editText.setText(name);
            editText2.setText(text);
            int i  = intent.getIntExtra("i", 0);

            mImageView.setImageBitmap(MainActivity.PictureAd.get(i));
            mImageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);

        }
    }

    public void select (View view){


        //izin verilmedi ise bu kod yazılır. kullanıcdan izin isetemek için.

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED){

            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2);


        }
        else
        //izin varsa bu kod çalışır.
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //bir sonuç için intent yapılıyor.

            startActivityForResult(intent, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 2 ){

            if(grantResults.length  >0 && grantResults[0] == PERMISSION_GRANTED){

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


        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                mImageView.setImageBitmap(mImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (requestCode == RESULT_CANCELED){

            Toast.makeText(getApplicationContext(),
                    "Kamera Desteklemiyor yada ilem iptal",
                    Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void save (View view){
        String pictureName =editText.getText().toString();
        String pictureNote = editText2.getText().toString();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        byte[] byteArray = outputStream.toByteArray();

        if(editText.getText().toString().trim().length() == 0 && editText2.getText().toString().trim().length() ==0  ) {

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


                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();

            }   catch (Exception e) {

                e.printStackTrace();
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){

            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }

        if(item.getItemId() == R.id.take_picture) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.i("LOG", "IOException");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }

        return super.onOptionsItemSelected(item);

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG","Permission is granted");
                return true;
            } else {

                Log.v("TAG","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG","Permission is granted");
            return true;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
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

        //Glide.with(getApplicationContext()).load(Camera()).into(mImageView);

        Glide.with(this)
                .load(R.drawable.background)
                .into(background);


    }
}
