package dev.div0.muribaqrguide.speaking;

import dev.div0.muribaqrguide.MainActivity;

public class SpeakerEventListener implements ISpeakerEventsReceiver {

    private MainActivity receiverActivity;

    public SpeakerEventListener(MainActivity _receiverActivity){
        receiverActivity = _receiverActivity;
    }

    @Override
    public void onDefaultLanguage(String lang) {
        receiverActivity.onDefaultLanguage(lang);
    }

    @Override
    public void onSpeakStarted() {
        receiverActivity.onSpeakStarted();
    }

    @Override
    public void onSpeakFinished() {
        receiverActivity.onSpeakFinished();
    }

    @Override
    public void onSpeakError(String utteranceId) {
        receiverActivity.onSpeakError(utteranceId);
    }
}
