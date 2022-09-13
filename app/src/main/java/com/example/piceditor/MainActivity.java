package com.example.piceditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.piceditor.databinding.ActivityMainBinding;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1234;
    private ActivityMainBinding binding;
    private static final String[] PERMISSIONS ={
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_COUNT = 2;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean notPermission(){
        for(int i=0;i < PERMISSION_COUNT;i++){
            if(checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        init();



    }


    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notPermission()){
            requestPermissions(PERMISSIONS,REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS && grantResults.length >0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(notPermission()){
                    ((ActivityManager) this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                }
            }
        }
    }


    private static final int REQUEST_PICK_IMAGE = 1234;
    private Uri imageUri;
    private boolean editMode = false;
    private Bitmap bitmap,bitmapEdit;
    private int width = 0;
    private int height = 0;
    private static final  int MAX_PIXEL_COUNT =2048;
    private int[] pixels;
    private int pixelCount = 0;

    private void init(){

        binding.selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                final Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                final Intent chooserIntent = Intent.createChooser(intent,"Select Image");
                startActivityForResult(chooserIntent,REQUEST_PICK_IMAGE);
            }
        });
        binding.featureCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();

                new Thread(){
                    public void run(){
                        if (bitmap.getWidth() >= bitmap.getHeight()){

                            bitmapEdit = Bitmap.createBitmap(
                                    bitmap,
                                    bitmap.getWidth()/2 - bitmap.getHeight()/2,
                                    0,
                                    bitmap.getHeight(),
                                    bitmap.getHeight()
                            );

                        }else{

                            bitmapEdit = Bitmap.createBitmap(
                                    bitmap,
                                    0,
                                    bitmap.getHeight()/2 - bitmap.getWidth()/2,
                                    bitmap.getWidth(),
                                    bitmap.getWidth()
                            );
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.imageView.setImageBitmap(bitmapEdit);
                            }
                        });
                    }
                }.start();


            }
        });
        binding.featureFlipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    public void run(){
                        Matrix matrix = new Matrix();
                        matrix.postScale( 1f,  -1f, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
                        bitmap= Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 binding.imageView.setImageBitmap(bitmap);
                             }
                         });
                    }
                }.start();

            }
        });
        binding.featureFlipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    public void run(){
                        Matrix matrix = new Matrix();
                        matrix.postScale( -1f,  1f, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
                        bitmap= Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
            }
        });
        binding.featureSaveGalary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                char[] chars = "0123456789".toCharArray();
                StringBuilder sb = new StringBuilder(20);
                Random random = new Random();
                for (int i = 0; i < 10; i++) {
                    char c = chars[random.nextInt(chars.length)];
                    sb.append(c);
                }
                String output  = sb.toString();
                String path =  "PicEditor-" + output + ".png";

                File imagePath = new File(Environment.getExternalStorageDirectory() + "/Pictures" +path );
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(imagePath);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e("GRECERRRERERERERRE", e.getMessage(), e);
                } catch (IOException e) {
                    Log.e("GREC", e.getMessage(), e);
                }
                Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_SHORT).show();
                MaterialDialog mDialog = new MaterialDialog.Builder(MainActivity.this)
                        .setTitle("Alert")
                        .setMessage("Pictures saved in Pictures Folder ")
                        .setCancelable(false)
                        .setPositiveButton("Got it", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Start again",new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface dialogInterface, int which) {
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        })
                        .build();
                // Show Dialog
                mDialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==REQUEST_PICK_IMAGE){
            imageUri = data.getData();
        }
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,"Loading","Please Wait",true);

        editMode = true;
        findViewById(R.id.welcome_screen).setVisibility(View.GONE);
        findViewById(R.id.edit_screen).setVisibility(View.VISIBLE);

        new Thread(){
          public void run(){
              bitmap =null;
              final BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
              bmpOptions.inBitmap = bitmap;
              bmpOptions.inJustDecodeBounds = true;
              try(InputStream input = getContentResolver().openInputStream(imageUri)){
                  bitmap = BitmapFactory.decodeStream(input,null,bmpOptions);
              } catch (IOException e) {
                  e.printStackTrace();
              }
              bmpOptions.inJustDecodeBounds =false;
              width = bmpOptions.outWidth;
              height = bmpOptions.outHeight;
              int resizeScale = 1;
              if(width>MAX_PIXEL_COUNT){
                  resizeScale = width/MAX_PIXEL_COUNT;

              }else if(height>MAX_PIXEL_COUNT){
                  resizeScale = height / MAX_PIXEL_COUNT;
              }
              if(width /resizeScale > MAX_PIXEL_COUNT || height / resizeScale > MAX_PIXEL_COUNT){
                  resizeScale++;
              }
              bmpOptions.inSampleSize = resizeScale;
              InputStream inputStream = null;
              try{
                  inputStream = getContentResolver().openInputStream(imageUri);
              } catch (FileNotFoundException e) {
                  e.printStackTrace();
                  recreate();
                  return;
              }
              bitmap = BitmapFactory.decodeStream(inputStream, null,bmpOptions);
              runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      binding.imageView.setImageBitmap(bitmap);
                      dialog.cancel();
                  }
              });
              width = bitmap.getWidth();
              height = bitmap.getHeight();
              bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);

              pixelCount = width * height;
              pixels = new int[pixelCount];
              bitmap.getPixels(pixels,0,width,0,0,width,height);

          }
        }.start();

    }
}