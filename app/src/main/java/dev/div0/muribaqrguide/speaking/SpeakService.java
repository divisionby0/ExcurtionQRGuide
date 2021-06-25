package dev.div0.muribaqrguide.speaking;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.UUID;

import dev.div0.muribaqrguide.socket.AppSocket;

public class SpeakService extends Service {

    private String tag = "SpeakService";
    private ISpeakerEventsReceiver eventsReceiver;
    private TextToSpeech TTS;
    private float speechSpeed = (float)0.92;
    private Locale locale;
    private boolean ready = false;
    private String textToSpeak;
    private String currentLang;

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public SpeakService getService() {
            return SpeakService.this;
        }
    }

    public void setEventReceiver(ISpeakerEventsReceiver _eventsReceiver){
        eventsReceiver = _eventsReceiver;
        initTTS();
    }

    public boolean say(String content){
        log("saying content="+content);
        log("saying with locale "+locale);
        stop();
        if(ready){
            textToSpeak = content;
            String utteranceId = UUID.randomUUID().toString();
            TTS.speak(content, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
        return ready;
    }
    public void stop(){
        if(ready && TTS.isSpeaking()){
            TTS.stop();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    // execution of service will start
    // on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {
        // returns the status
        // of the program
        //return START_STICKY;

        log("onStartCommand");
        textToSpeak = intent.getStringExtra("textToSpeak");
        currentLang = intent.getStringExtra("lang");

        eventsReceiver = (ISpeakerEventsReceiver) intent.getSerializableExtra("eventsReceiver");

        log("textToSpeak="+textToSpeak);
        log("currentLang="+currentLang);
        log("eventsReceiver="+eventsReceiver);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    // execution of the service will
    // stop on calling this method
    public void onDestroy() {
        super.onDestroy();
        TTS.stop();
        TTS.shutdown();
    }

    private void initTTS(){
        log("initTTS()");

        final String lang = Locale.getDefault().getLanguage();

        log("lang:"+lang);
        TTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int initStatus) {
                Log.d(tag, "onInit lang="+lang);
                TTS.setSpeechRate(speechSpeed);
                initLang(lang);
                //eventsReceiver.onDefaultLanguage(lang);
            }
        });

        TTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                eventsReceiver.onSpeakFinished();
            }

            @Override
            public void onError(String utteranceId) {
                eventsReceiver.onSpeakError(utteranceId);
            }

            @Override
            public void onStart(String utteranceId) {
                eventsReceiver.onSpeakStarted();
            }
        });
    }

    private Locale getLocale(String lang) {
        boolean isRUS = lang.indexOf("ru")!=-1;
        boolean isENG = lang.indexOf("en")!=-1;
        boolean isUKR = lang.indexOf("uk")!=-1;
        boolean isFRA = lang.indexOf("fr")!=-1;
        Locale defaultLocale = new Locale("ru","RUS");
        Locale locale;

        if(isENG){
            locale = new Locale("en","ENG");
        }
        else if(isUKR){
            locale = new Locale("uk","UA");
        }
        else if(isFRA){
            locale = Locale.FRANCE;
        }
        else if(isRUS){
            locale = defaultLocale;
        }
        else{
            locale = defaultLocale;
        }
        return locale;
    }

    private void initLang(String lang){
        locale = getLocale(lang);

        log("initLang() locale:"+locale);

        int result = TTS.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA) {
            log("Missing language data for locale "+lang);
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            log("Language not supported "+lang);
        } else {
            Locale currentLanguage = TTS.getVoice().getLocale();
            ready = true;
            log("TTS language "+currentLanguage+" inited");
        }
    }

    private void log(String data){
        Log.d(tag, data);

        if(AppSocket.getInstance().isConnected()){
            AppSocket.getInstance().sendLog(data);
        }
    }
}
