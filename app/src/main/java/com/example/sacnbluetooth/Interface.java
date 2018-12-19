package com.example.sacnbluetooth;

import android.speech.SpeechRecognizer;

/**
 * Created by Meng on 2017/11/7.
 */

public interface Interface {
    SpeechRecognizer getSpeechRecognizer();             //
    void onResults(String result);
    void onPartialResults(String partialResult);
}
