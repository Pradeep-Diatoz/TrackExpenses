package com.example.trackyourexpense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageReceiver extends BroadcastReceiver {

    private static MessageListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = bundle.getString("format");
                        currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i], format);
                        Log.e("Current Message", format + " : " + currentMessage.getDisplayOriginatingAddress());
                    } else {
                        currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    }
                    Pattern regEx =
                            Pattern.compile("[a-zA-Z0-9]{2}-[a-zA-Z0-9]{6}");
                    Matcher m = regEx.matcher(currentMessage.getDisplayOriginatingAddress());
                    if (m.find()) {
                        try {
                            Pattern regExForAm
                                    = Pattern.compile("(?:inr|rs)+[\\s]*[0-9+[\\,]*+[0-9]*]+[\\.]*[0-9]+");
                            // Find instance of pattern matches
                            Matcher matchAm = regExForAm.matcher(currentMessage.getMessageBody());
                            if (m.find()) {
                                try {
                                    Log.e("amount_value= ", "" + m.group(0));
                                    String amount = (m.group(0).replaceAll("inr", ""));
                                    amount = amount.replaceAll("rs", "");
                                    amount = amount.replaceAll("inr", "");
                                    amount = amount.replaceAll(" ", "");
                                    amount = amount.replaceAll(",", "");
                                    if (currentMessage.getMessageBody().contains("debited") ||
                                            currentMessage.getMessageBody().contains("purchasing") || currentMessage.getMessageBody().contains("purchase")
                                            || currentMessage.getMessageBody().contains("dr")) {
                                        mListener.messageReceived(amount);
                                    } else if (currentMessage.getMessageBody().contains("credited") || currentMessage.getMessageBody().contains("cr")) {

                                    }
                                    Log.e("matchedValue= ", "" + amount);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e("No_matchedValue ", "No_matchedValue ");
                            }
                            String phoneNumber = m.group(0);
                            Long date = currentMessage.getTimestampMillis();
                            String message = currentMessage.getDisplayMessageBody();
                            Log.e("SmsReceiver Mine", "senderNum: " + phoneNumber + "; message: " + message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("Mismatch", "Mismatch value");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }

    }

    public static void bindListener(MessageListener listener) {
        mListener = listener;
    }
}
