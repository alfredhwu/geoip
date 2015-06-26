package com.accenture.cnooclab;

import com.accenture.cnooclab.beans.IPGeoLocation;
import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.validation.annotation.ValidateString;
import com.github.thorqin.toolkit.web.HttpException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebModule
public class MyModule {

    @Service("db")
    DBService db;

    @WebEntry(method = HttpMethod.POST)
    public WebContent getServerInfo(HttpServletRequest request) {
        String serverTime = new DateTime().toString(DateTimeFormat.mediumDateTime());
        String serverName = request.getServletContext().getServerInfo();
        return WebContent.json(serverName + "<br>" + serverTime);
    }

    @WebEntry(method = HttpMethod.GET)
    public WebContent getIPGeoLocation(@Param("ip") @ValidateString(ValidateString.IPV4) String ip) {
        IPGeoLocation ipgeo = this.getIPGeoLocationFromDB(ip);
        // if not found, retrieve from API
        if (null == ipgeo) {
            ipgeo = this.getIPGeoLocationFromAPI(ip);
            // cache IP GeoLocation into DB
            this.cacheIPGeoLocationIntoDB(ipgeo);
        }
        if (null != ipgeo) {
            return WebContent.json(ipgeo);
        } else {
            throw new HttpException(404, "Geo locaion of IP [" + ip + "] not found !");
            //return WebContent.jsonString("{ 'errcode' : 2, 'message' : 'Geo locaion of IP [" + ip + "] not found !' }");
        }
    }

    private long getIPHash(String ip) {
        String ipString = ip.trim();
//        String ipPattern = "([1-9]|[1-9]//d|1//d{2}|2[0-4]//d|25[0-5])(//.(//d|[1-9]//d|1//d{2}|2[0-4]//d|25[0-5])){3}";
//        Pattern pattern = Pattern.compile(ipPattern);
//        Matcher matcher = pattern.matcher(ipString);
//        if (!matcher.matches()) {
//            return -1;
//        }
        long ipHash = 0;
        for (String segment : ipString.split("\\.")) {
            ipHash = ipHash * 256 + Integer.parseInt(segment);
        }
        return ipHash;
    }

    final long IP_MAX = 4294967295L;

    private String getIPString(long iphash) {
        if (iphash < 0 || iphash > IP_MAX) {
            return null;
        }
        Long residue = iphash / 256;
        String ip = String.valueOf(iphash % 256);
        for (int i=0; i<3; i++) {
            ip = String.valueOf(residue % 256) + "." + ip;
            residue /= 256;
        }
        return ip;
    }

    private IPGeoLocation getIPGeoLocationFromDB(String ip) {
        WebApplication.get().getLogger().info("Method getIPGeoLocationFromDB called");

//        db.execute("sql");
        long ipHash = this.getIPHash(ip);
        IPGeoLocation ipgeo = null;
        String sql = "select * from ip_geo_location where ip = ?";
        try {
//            db.execute(sql);
//            db.queryTable(sql).
            ipgeo = db.queryFirst(sql, IPGeoLocation.class, ipHash);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ipgeo;
    }

    private IPGeoLocation getIPGeoLocationFromAPI(String ip){

        // http client
        // url connection
        final String API_URL = WebApplication.get().getConfigManager().getString("ip_api/url");
        final String PROXY_SERVER = WebApplication.get().getConfigManager().getString("proxy/server");
        final int PROXY_PORT = WebApplication.get().getConfigManager().getInteger("proxy/port");
        final String PROXY_USER = WebApplication.get().getConfigManager().getString("proxy/user");
        final String PROXY_PASSWD = WebApplication.get().getConfigManager().getString("proxy/passwd");
        IPGeoLocation ipGeoLocation = null;

        try {
            WebApplication.get().getLogger().info("Call API to retrieve geo information about IP [" + ip + "]");

            URL url = new URL(API_URL+"/"+ip);

            // proxy settings
            InetSocketAddress proxyServerAddr = new InetSocketAddress(PROXY_SERVER, PROXY_PORT);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyServerAddr);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(proxy);

            String encoded = new String(Base64.getEncoder().encodeToString(new String(PROXY_USER+":"+PROXY_PASSWD).getBytes("utf-8")));
            urlConnection.setRequestProperty("Proxy-Authorization", "Basic "+encoded);
            urlConnection.setReadTimeout(5 * 1000);
            urlConnection.setRequestMethod("GET");

            InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream(), "utf-8");
            Scanner inputStream = new Scanner(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();

            while (inputStream.hasNext()) {
                stringBuffer.append(inputStream.nextLine());
            }

            if (urlConnection.getResponseCode()==200 && urlConnection.getContentType().contains("application/json")) {
                ipGeoLocation =  this.parseAPIResult(stringBuffer.toString());
                ipGeoLocation.setIp(this.getIPHash(ip));
            }
            // parse stringBuffer
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ipGeoLocation;
    }

    private IPGeoLocation parseAPIResult(String jsonResult) {
        ArrayList<String> result = new ArrayList<String>();
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(jsonResult).getAsJsonArray();
        for (JsonElement element : jsonArray) {
            result.add(element.getAsString());
        }
        return new IPGeoLocation(-1L, result.get(0), result.get(1), result.get(2), result.get(3), result.get(4));
    }

    private void cacheIPGeoLocationIntoDB(IPGeoLocation ipgeo) {
        String sql = "INSERT INTO ip_geo_location (ip, nation, province, city, institute, isp) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            this.db.execute(sql,
                    ipgeo.getIp(),
                    ipgeo.getNation(),
                    ipgeo.getProvince(),
                    ipgeo.getCity(),
                    ipgeo.getInstitute(),
                    ipgeo.getISP());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}