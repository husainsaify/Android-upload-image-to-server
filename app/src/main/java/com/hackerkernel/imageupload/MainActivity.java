package com.hackerkernel.imageupload;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity {
    //define global views variable
    public ImageView imageView;
    public Button   selectImage,
                    uploadImage;
    public String SERVER = "http://192.168.1.2/co/storemanger/saveImage.php",
                    timestamp;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RESULT_SELECT_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instantiate view
        imageView = (ImageView) findViewById(R.id.imageView);
        selectImage = (Button) findViewById(R.id.selectImage);
        uploadImage = (Button) findViewById(R.id.uploadImage);

        //when selectImage button is pressed
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call the function to select image from album
                selectImage();
            }
        });

        //when uploadImage button is pressed
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get image in bitmap format
                Bitmap image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                //execute the async task and upload the image to server
                new Upload(image,"IMG_"+timestamp).execute();
            }
        });

    }

    //function to select a image
    private void selectImage(){
        //open album to select image
        Intent gallaryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallaryIntent, RESULT_SELECT_IMAGE);
    }

    /*
    * This function is called when we pick some image from the album
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_SELECT_IMAGE && resultCode == RESULT_OK && data != null){
            //set the selected image to image variable
            Uri image = data.getData();
            imageView.setImageURI(image);

            //get the current timeStamp and strore that in the time Variable
            Long tsLong = System.currentTimeMillis() / 1000;
            timestamp = tsLong.toString();

            Toast.makeText(getApplicationContext(),timestamp,Toast.LENGTH_SHORT).show();
        }
    }

    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    //async task to upload image
    private class Upload extends AsyncTask<Void,Void,String>{
        private Bitmap image;
        private String name;

        public Upload(Bitmap image,String name){
            this.image = image;
            this.name = name;
        }

        @Override
        protected String doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //compress the image to jpg format
            image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            /*
            * encode image to base64 so that it can be picked by saveImage.php file
            * */
            String encodeImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);

            //generate hashMap to store encodedImage and the name
            HashMap<String,String> detail = new HashMap<>();
            detail.put("name", name);
            detail.put("image", encodeImage);

            try{
                //convert this HashMap to encodedUrl to send to php file
                String dataToSend = hashMapToUrl(detail);
                //make a Http request and send data to saveImage.php file
                String response = Request.post(SERVER,dataToSend);

                //return the response
                return response;

            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"ERROR  "+e);
                return null;
            }
        }



        @Override
        protected void onPostExecute(String s) {
            //show image uploaded
            Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();
        }
    }
}
