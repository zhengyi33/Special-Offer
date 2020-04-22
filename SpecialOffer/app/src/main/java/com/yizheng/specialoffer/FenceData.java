package com.yizheng.specialoffer;

import java.io.Serializable;

class FenceData implements Serializable {

    private String id;
    private String address;
    private String website;
    private float radius;
    private int type;
    private double lat;
    private double lon;
    private String message;
    private String code;
    private String fenceColor;
    private String logo;

    FenceData(String id, String address, String website, float radius, int type, double lat, double lon, String message, String code, String fenceColor, String logo) {
        this.id = id;
        this.address = address;
        this.website = website;
        this.radius = radius;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.message = message;
        this.code = code;
        this.fenceColor = fenceColor;
        this.logo = logo;
    }

     String getId() {
        return id;
    }

     String getAddress() {
        return address;
    }

     String getWebsite() {
        return website;
    }

     float getRadius() {
        return radius;
    }

     int getType() {
        return type;
    }

     double getLat() {
        return lat;
    }

     double getLon() {
        return lon;
    }

     String getMessage() {
        return message;
    }

     String getCode() {
        return code;
    }

     String getFenceColor() {
        return fenceColor;
    }

     String getLogo() {
        return logo;
    }
}
