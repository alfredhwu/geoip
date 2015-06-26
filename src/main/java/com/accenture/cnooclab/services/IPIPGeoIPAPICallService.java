package com.accenture.cnooclab.services;

import com.github.thorqin.toolkit.web.WebApplication;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Base64;
import java.util.Scanner;

/**
 * Created by dingwen.wu on 6/26/2015.
 */
public class IPIPGeoIPAPICallService extends APICallService {

    public IPIPGeoIPAPICallService() {

    }

    public String callRemoteAPI(Object...args) {
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
        return null;
    }
}
