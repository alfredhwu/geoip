package com.accenture.cnooclab.services;

import com.github.thorqin.toolkit.web.WebApplication;

/**
 * Created by dingwen.wu on 6/26/2015.
 */
public abstract class APICallService implements APICallInterface{
    final String USER_PROXY = WebApplication.get().getConfigManager().getString("proxy/useproxy");
    final String PROXY_SERVER = WebApplication.get().getConfigManager().getString("proxy/server");
    final int PROXY_PORT = WebApplication.get().getConfigManager().getInteger("proxy/port");
    final String PROXY_USER = WebApplication.get().getConfigManager().getString("proxy/user");
    final String PROXY_PASSWD = WebApplication.get().getConfigManager().getString("proxy/passwd");
}
