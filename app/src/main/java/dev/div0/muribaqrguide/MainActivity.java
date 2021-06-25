package dev.div0.muribaqrguide;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import dev.div0.muribaqrguide.route.point.PointParser;
import dev.div0.muribaqrguide.route.point.PointParserException;
import dev.div0.muribaqrguide.route.point.RGPoint;
import dev.div0.muribaqrguide.socket.AppSocket;
import dev.div0.muribaqrguide.socket.ISocketListenerCallback;
import dev.div0.muribaqrguide.speaking.ISpeakerEventsReceiver;
import dev.div0.muribaqrguide.speaking.SpeakService;
import dev.div0.muribaqrguide.speaking.SpeakerEventListener;
import dev.div0.muribaqrguide.utils.ToastUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ICallbacks, ISpeakerEventsReceiver, ISocketListenerCallback, AdapterView.OnItemSelectedListener {

    private String tag = "MainActivityDebug";
    private Button buttonScan;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private ImageView pointImage;
    private Spinner languagesSelector;

    //qr code scanner object
    private IntentIntegrator qrScan;

    //private SpeakManager speakManager;
    private String[] languages = { "ru_RU", "en_EN", "uk_UA", "fr_FR"};

    private String currentLang;
    private int selectedPointId = -1;
    private SpeakerEventListener speakerEventListener;

    private SpeakService speakService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        AppSocket.getInstance().setCallbacks(this);

        buttonScan = (Button) findViewById(R.id.buttonScan);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        pointImage = (ImageView) findViewById(R.id.pointImage);

        pointImage.setBackgroundColor(Color.rgb(0, 0, 0));

        buttonScan.setOnClickListener(this);

        createScanner();
        createLanguageSelector();
        createSpeakerListener();
        createSocket();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createScanner() {
        qrScan = new IntentIntegrator(this);
        qrScan.setBeepEnabled(false);
        qrScan.setOrientationLocked(true);
    }

    private void createLanguageSelector(){
        languagesSelector = (Spinner) findViewById(R.id.languageSelector);
        languagesSelector.setOnItemSelectedListener(this);

        ArrayAdapter languagesAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, languages);
        languagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languagesSelector.setAdapter(languagesAdapter);
    }

    private void createSpeakerListener(){
        speakerEventListener = new SpeakerEventListener(this);
    }


    private void createSocket() {
        Log.d(tag,"createSocket");
        final MainActivity that = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppSocket.getInstance().setUserId("777");
                AppSocket.getInstance().setDeviceInfo("deviceInfo");
                AppSocket.getInstance().setSocketListenerCallback(that);
                AppSocket.getInstance().init();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                selectedPointId = Integer.parseInt(result.getContents());

                Log.d(tag, "point id: "+selectedPointId);
                getPointData(selectedPointId);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        qrScan.initiateScan();
    }

    @Override
    public void onPoint(String pointData) {
        Log.d(tag, "onPoint");

        final MainActivity that = this;

        try {
            Log.d(tag, "parsing point...");
            RGPoint point = PointParser.parse(pointData);

            final String name = point.getName();
            final String descr = point.getDescription();
            final String image = getResources().getString(R.string.imagesBaseUrl) + point.getId()+".jpg";

            Log.d(tag, "name:"+point.getName());
            Log.d(tag, "description:"+point.getDescription());
            Log.d(tag, "image:"+image);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nameTextView.setText(name);
                    descriptionTextView.setText(descr);

                    Picasso.get().load(image).into(pointImage);
                    pointImage.setVisibility(View.VISIBLE);

                    Intent mIntent = new Intent(that, SpeakService.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putString("textToSpeak", descr);
                    mBundle.putString("lang", currentLang);
                    //mBundle.putSerializable("eventsReceiver", speakerEventListener);
                    mIntent.putExtras(mBundle);

                    startService(mIntent);

                    //speakManager.say(descr);
                }
            });
        } catch (PointParserException e) {
            e.printStackTrace();
        }
    }

    private void getPointData(final int pointId){
        Log.d(tag, "getting point "+pointId+" currentLang="+currentLang+" data...");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AppSocket.getInstance().getPointById(pointId, currentLang);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSocketConnected() {
    }

    @Override
    public void onSocketDisconnected() {
    }

    @Override
    public void onSocketConnectError(String error) {
    }

    @Override
    public void onYandexAPIKey(String key) {
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        currentLang = languages[position];
        //speakManager.setLanguage(currentLang);

        if(selectedPointId!=-1){
            getPointData(selectedPointId);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onDefaultLanguage(String lang) {
        Log.d(tag, "onDefaultLanguage "+lang);
        int defaultLanguageIndex = getDefaultLanguageIndex(lang);
        Log.d(tag, "default lang index: "+defaultLanguageIndex);
        languagesSelector.setSelection(defaultLanguageIndex);
    }

    @Override
    public void onSpeakStarted() {
        Log.d(tag, "Speak started");
        //ToastUtils.show(this,"TTS started");
    }

    @Override
    public void onSpeakError(String utteranceId) {
        Log.d(tag, "Speak phrase with id "+utteranceId+" error");
    }

    @Override
    public void onSpeakFinished() {
        Log.d(tag, "Speak finished");
        //ToastUtils.show(this,"TTS finished");
    }

    private int getDefaultLanguageIndex(String lang){
        int index = -1;
        int i;
        int total = languages.length;
        for(i=0 ;i<total; i++){
            String currentLang = languages[i];

            if(currentLang.indexOf(lang)!=-1){
                return i;
            }
        }
        return index;
    }
}