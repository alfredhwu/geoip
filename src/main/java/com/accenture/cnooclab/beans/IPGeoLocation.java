package com.accenture.cnooclab.beans;

import com.github.thorqin.toolkit.db.DBService;

/**
 * Created by dingwen.wu on 6/25/2015.
 */
public class IPGeoLocation {
    @DBService.DBField
    private long ip;
    @DBService.DBField
    private String nation;
    @DBService.DBField
    private String province;
    @DBService.DBField
    private String city;
    @DBService.DBField
    private String institute;
    @DBService.DBField
    private String ISP;
    private int longitude;
    private int latitude;

    public IPGeoLocation() {
        this.longitude = -1;
        this.latitude = -1;
    }

    public IPGeoLocation(long ip, String nation, String province, String city, String institute, String ISP) {
        this.ip = ip;
        this.nation = nation;
        this.province = province;
        this.city = city;
        this.institute = institute;
        this.ISP = ISP;
        this.latitude = -1;
        this.longitude = -1;
    }

    public long getIp() {
        return this.ip;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }

    public String getNation() {
        return this.nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInstitute() {
        return this.institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getISP() {
        return ISP;
    }

    public void setISP(String ISP) {
        this.ISP = ISP;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }
}
