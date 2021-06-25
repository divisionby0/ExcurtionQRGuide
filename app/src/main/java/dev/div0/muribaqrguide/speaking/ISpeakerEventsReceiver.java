package dev.div0.muribaqrguide.speaking;

import java.io.Serializable;

public interface ISpeakerEventsReceiver extends Serializable {
    void onDefaultLanguage(String lang);
    void onSpeakStarted();
    void onSpeakFinished();
    void onSpeakError(String utteranceId);
}
