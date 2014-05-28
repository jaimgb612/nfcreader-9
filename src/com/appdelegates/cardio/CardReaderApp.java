package com.appdelegates.cardio;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by Helios on 5/14/2014.
 */
public class CardReaderApp implements CardReader.CardReaderListener{

    CardReader cardReader;
    private final String USER_AGENT = "Mozilla/5.0";
    private String mLastCardRead="";

    public CardReaderApp(){

        cardReader = new CardReader(this, false);
    }

    private void sendGetRequest(String hexval)  {

        String url = "http://localhost:3030/newuser/"+hexval;

        URL obj = null;
        try {
            obj = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println("[BAD ERROR] Malformed URL in GET method. Contact Mitch because he fucked up!");
            return;
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            System.out.println("[ERROR] Something bad happened creating a connection. Maybe the HOST is dead?");
            return;
        }

        // optional default is GET
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            System.out.println("[ERROR] Malformed Request Method in GET method. Contact Mitch because he fucked up!");
            return;
        }

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            System.out.println("[ERROR] Error getting response code. Is the YTLB app running on this machine?");
            return;
        }

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        //print result
    }

    @Override
    public void error(String e) {
        System.out.println("ERROR: "+e);
    }

    @Override
    public void newCardRead(String hexval) {
        System.out.println("Read card: "+ hexval);

        if (!hexval.equalsIgnoreCase(mLastCardRead)){
            mLastCardRead = hexval;
            sendGetRequest(hexval);
        } else {
            System.out.println("Duplicate card in last 15 seconds: "+hexval);
            (new ClearID()).start();
        }

    }

    private class ClearID extends Thread {

        public void run() {

            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mLastCardRead = "";
        }
    }

}
