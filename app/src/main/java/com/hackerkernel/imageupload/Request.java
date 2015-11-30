package com.hackerkernel.imageupload;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * class to make Http Request to the web
 */
public class Request {

    private static final String TAG = Request.class.getSimpleName();

    public static String post(String serverUrl,String dataToSend){
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            //set timeout of 30 seconds
            con.setConnectTimeout(1000 * 30);
            con.setReadTimeout(1000 * 30);
            //method
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

            //make request
            writer.write(dataToSend);
            writer.flush();
            writer.close();
            os.close();

            //get the response
            int responseCode = con.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                //read the response
                StringBuilder sb = new StringBuilder();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String line;

                //loop through the response from the server
                while ((line = reader.readLine()) != null){
                    sb.append(line).append("\n");
                }

                //return the response
                return sb.toString();
            }else{
                Log.e(TAG,"ERROR - Invalid response code from server "+ responseCode);
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"ERROR "+e);
            return null;
        }
    }
}
