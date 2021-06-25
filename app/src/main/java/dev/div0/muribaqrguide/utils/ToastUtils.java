package dev.div0.muribaqrguide.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtils {
    static Toast toast = null;

    public static void show(Context context, String text) {
        try {
            if(toast!=null){
                toast.setText(text);
            }else{
                toast= Toast.makeText(context, text, Toast.LENGTH_SHORT);
            }
            toast.show();
        } catch (Exception e) {
            // Resolve the exception handling of calling Toast in the child thread
            Looper.prepare();
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

}
