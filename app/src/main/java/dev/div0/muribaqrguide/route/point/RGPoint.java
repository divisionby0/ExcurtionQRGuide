package dev.div0.muribaqrguide.route.point;

import android.graphics.Point;

public class RGPoint {
    private int id;
    private int index;
    private Point position;
    private String name;
    private String description;
    private Integer distance;
    private int color;
    private int textColor;

    public RGPoint(int _id, int _index, Point _position, String _name, String _description, Integer _distance){
        id = _id;
        index = _index;
        position = _position;
        name = _name;
        description = _description;
        distance = _distance;
    }

    public Point getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDistance() {
        return distance;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getIndex() {
        return index;
    }

    public int getId() {
        return id;
    }
}
