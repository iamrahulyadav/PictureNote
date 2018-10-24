package com.example.mac.picturenote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoEditor extends AppCompatActivity {

    PhotoEditorView     mPhotoEditorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);


        PhotoEditorView mPhotoEditorView = findViewById(R.id.photoEditorView);

        mPhotoEditorView.getSource().setImageResource(R.drawable.got);

    }
}
