package dev.div0.muribaqrguide.speaking;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

import dev.div0.muribaqrguide.socket.AppSocket;

public class SpeakManager {
    private Context context;
    private String tag = "SpeakManager";
    private TextToSpeech TTS;
    private boolean ready = false;
    private ISpeakerEventsReceiver eventsReceiver;
    private Locale locale;
    private String textToSpeak;
    private float speechSpeed = (float)0.92;

    public SpeakManager(Context _context, ISpeakerEventsReceiver _eventsReceiver){
        eventsReceiver = _eventsReceiver;

        context = _context;

        final String lang = Locale.getDefault().getLanguage();

        log("lang:"+lang);
        TTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int initStatus) {
                log("SpeakManager onInit lang="+lang);
                TTS.setSpeechRate(speechSpeed);
                initLang(lang);
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

        eventsReceiver.onDefaultLanguage(lang);
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

    public void setLanguage(String lang){
        initLang(lang);
        stop();
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
        /*
        switch(lang){
            case "ru":
            default:
                locale = new Locale("ru","RU");
                break;
            case "en":
                locale = new Locale("en","US");
                break;
        }
         */
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
