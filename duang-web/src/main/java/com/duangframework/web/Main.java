package com.duangframework.web;

import com.duangframework.server.netty.server.Server;

/**
 * Created by laotang on 2017/11/5.
 */
public class Main {
    public static void main(String[] args) {
        Server server = new Server(9090);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
