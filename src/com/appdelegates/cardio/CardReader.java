package com.appdelegates.cardio;

import javax.smartcardio.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Helios on 5/14/2014.
 */
public class CardReader {

    public static boolean mVerbose = false;
    private CardTerminal mTerminal = null;
    String mLastCardRead = "";
    CardReaderListener mListener = null;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();


    public interface CardReaderListener {
        public void error(String e);
        public void newCardRead(String hexval);
    }

    private void signalError(String e){
        if (mListener!=null)
            mListener.error(e);
    }

    private void signalCard(String cardID){
        if (mListener!=null)
            mListener.newCardRead(cardID);
    }

    public static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public CardReader(CardReaderListener listener, boolean verbose){
        mListener = listener;
        mVerbose = verbose;

        TerminalFactory factory = null;
        try {
            factory = TerminalFactory.getInstance("PC/SC", null);
        } catch (NoSuchAlgorithmException e) {
            signalError(e.toString());
        }

        if (verbose)
            System.out.println(factory);

        List<CardTerminal> terminals = null;
        try {
            terminals = factory.terminals().list();
        } catch (CardException e) {
            signalError(e.toString());
        }

        if (verbose)
            System.out.println("Terminals: " + terminals);

        if (terminals.isEmpty()) {
            signalError("No card terminals available");
        } else {
            mTerminal = terminals.get(0);
            CRThread crt = new CRThread();
            crt.start();
        }

    }

    public class CRThread extends Thread {

        public void run(){

            if (mVerbose)
                System.out.println("Card Reader Thread running...");

            while( true )
            {
                try {
                    mTerminal.waitForCardPresent( 0 );
                    Card card = mTerminal.connect("*");
                    CardChannel channel = card.getBasicChannel();

                    CommandAPDU command = new CommandAPDU(new byte[]{(byte)0xFF,(byte)0xCA,(byte)0x00,(byte)0x00,(byte)0x00});
                    ResponseAPDU response = channel.transmit(command);

                    byte[] byteArray = response.getBytes();

                    String hexCard =  bytesToHex( byteArray );

                    // 18 is what we should get, 4 bytes are error codes
                    if (hexCard.length()== 18)
                        signalCard( hexCard.substring(0, 14) );
                    else
                        signalError("Invalid card read ["+hexCard+"]. This is usually caused by pulling the card too fast!");

                    if (mVerbose)
                        System.out.println( "In thread: "+ hexCard );

                    Thread.sleep(1000);

                } catch (CardException e) {
                    // This will fire if the card is removed too fast, not really useful
                    if (mVerbose)
                        e.printStackTrace();
                } catch (InterruptedException e) {
                    // Only called when timer is interrupted
                    if (mVerbose)
                        e.printStackTrace();
                } catch (Exception e) {
                    if (mVerbose)
                        e.printStackTrace();
                }

            }
        } // end of run()

    }

}
