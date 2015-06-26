package com.accenture.cnooclab;

import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.web.HttpException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import com.github.thorqin.toolkit.web.annotation.WebEntry.HttpMethod;
import com.github.thorqin.toolkit.web.utility.ServletUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Base64;
import java.util.Scanner;

@WebModule
public class MyModule {
    @DBInstance
    DBService db;

    @WebEntry(method = HttpMethod.POST)
    public WebContent getServerInfo(HttpServletRequest request) {
        String serverTime = new DateTime().toString(DateTimeFormat.mediumDateTime());
        String serverName = request.getServletContext().getServerInfo();
        return WebContent.json(serverName + "<br>" + serverTime);
    }

    @WebEntry(method = HttpMethod.GET)
    public WebContent getIPGeoLocation(@Param("ip") String ip) {
        IPGeoLocation ipgeo = this.getIPGeoLocationFromDB(ip);
        // if not found, retrieve from API
        if (null == ipgeo) {
            ipgeo = this.getIPGeoLocationFromAPI(ip);
        }
        if (null != ipgeo) {
            // cache IP GeoLocation into DB
            this.cacheIPGeoLocationIntoDB(ip, ipgeo);

            return WebContent.json(ipgeo);
        } else {
            throw new HttpException(404, "Geo locaion of IP [" + ip + "] not found !");
            //return WebContent.jsonString("{ 'errcode' : 2, 'message' : 'Geo locaion of IP [" + ip + "] not found !' }");
        }
    }

    private IPGeoLocation getIPGeoLocationFromDB(String ip) {

//        db.execute("sql");
        //postgres


        //
//        Serializer.fromJson("", IPGeoLocation.class);
        IPGeoLocation ipgeo = new IPGeoLocation(ip, "中国", "天津", "天津");
        return null;
    }

    private IPGeoLocation getIPGeoLocationFromAPI(String ip){

        // http client
        // url connection
        String api_url = WebApplication.get().getConfigManager().getString("ipapi/url");
        IPGeoLocation ipGeoLocation = null;

        try {
            URL url = new URL(api_url);

            // proxy settings
            InetSocketAddress proxyServerAddr = new InetSocketAddress("10.68.211.1", 8080);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyServerAddr);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(proxy);

            String encoded = new String(Base64.getEncoder().encodeToString(new String("ex_wudw2:Accenture1").getBytes("utf-8")));
            urlConnection.setRequestProperty("Proxy-Authorization", "Basic "+encoded);
            urlConnection.setReadTimeout(5 * 1000);
            urlConnection.setRequestMethod("GET");

            InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream(), "utf-8");
            Scanner inputStream = new Scanner(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();

            while (inputStream.hasNext()) {
                stringBuffer.append(inputStream.nextLine());
            }

            if (urlConnection.getResponseCode()==200 && urlConnection.getContentType()=="application/json") {
                ipGeoLocation =  Serializer.fromJson(stringBuffer.toString(), IPGeoLocation.class);
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

    private void cacheIPGeoLocationIntoDB(String ip, IPGeoLocation ipgeo) {
        // Todo: cache IP Geolocation into DB
    }
}

class IPGeoLocation {
    public String ip;
    public String nation;
    public String province;
    public String city;
    public String institute;
    public String ISP;
    public int longitude;
    public int latitude;

    public IPGeoLocation(String ip, String nation, String province, String city, String institute, String ISP) {
        this.ip = ip;
        this.nation = nation;
        this.province = province;
        this.city = city;
        this.institute = institute;
        this.ISP = ISP;
        this.longitude = 120;
        this.latitude = 32;
    }

    public IPGeoLocation(String ip, String nation, String province, String city) {
        this.ip = ip;
        this.nation = nation;
        this.province = province;
        this.city = city;
        this.institute = "";
        this.ISP = "";
        this.longitude = 120;
        this.latitude = 32;
    }
}
