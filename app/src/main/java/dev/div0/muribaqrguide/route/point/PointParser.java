package dev.div0.muribaqrguide.route.point;

import android.graphics.Point;

import org.json.JSONException;
import org.json.JSONObject;

public class PointParser {
    public static RGPoint parse(String pointData) throws PointParserException{

        JSONObject data = null;
        int pointId = -1;
        String name = "undefined";
        String description = "undefined";

        try {
            data = new JSONObject(pointData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(data!=null){

            try {
                pointId = data.getInt("pointId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                name = data.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                description = data.getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RGPoint point = new RGPoint(pointId, pointId, new Point(0,0), name, description, 0);

            return point;
        }
        else{
            throw new PointParserException("Error parsing point data");
        }
    }
}
