package com.example.sacnbluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;

public class MainActivity implements RecognitionListener {

    public static final int STATUS_None = 0;
    public static final int STATUS_WaitingReady = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;
    private int status = STATUS_None;
    private Interface mInterface;

    MainActivity(Interface callBack) {
        this.mInterface = callBack;
        //mInterface.getSpeechRecognizer().setRecognitionListener(this);
        // speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        mInterface.getSpeechRecognizer().setRecognitionListener(this);
    }

    public int getStatus(){
        if(status == STATUS_None){
            start();
            status = STATUS_WaitingReady;
        }else if(status == STATUS_WaitingReady || status == STATUS_Ready ||status == STATUS_Recognition ){
            cancel();
            status = STATUS_None;
        }
        else if(status == STATUS_Speaking ){
            stop();
            status = STATUS_Recognition;
        }
        return status;

    }


  /*  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(this);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "OK", Toast.LENGTH_SHORT).show();

            }
        });
    }*/


    private void start() {
        Intent intent = new Intent();
        mInterface.getSpeechRecognizer().startListening(intent);
    }

    private void stop() {
        mInterface.getSpeechRecognizer().stopListening();
    }

    private void cancel() {
        mInterface.getSpeechRecognizer().cancel();
        status = STATUS_None;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        status = STATUS_Ready;
    }

    @Override
    public void onBeginningOfSpeech() {
        status = STATUS_Speaking;
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        status = STATUS_Recognition;
    }

    @Override
    public void onError(int error) {
        status = STATUS_None;
    }

    @Override
    public void onResults(Bundle results) {
        status = STATUS_None;
        ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        /* line1.setVisibility(View.VISIBLE);
        try {
            os.write(txtResult.getText().toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        mInterface.onResults(result.get(0));
    }


    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> result = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (result.size() > 0) {
            //txtResult.setText(result.get(0));
            mInterface.onPartialResults(result.get(0));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }
}
