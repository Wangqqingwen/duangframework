package com.duangframework.server.netty.server;

import com.duangframework.mvc.listener.ContextLoaderListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author laotang
 * @date 2017/11/8
 */
public class DuangContextListener implements FutureListener<Void> {

    private BootStrap bootStrap;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DuangContextListener(BootStrap bootStrap) {
        this.bootStrap = bootStrap;
    }

    @Override
    public void operationComplete(Future<Void> future) throws Exception {
        if (future.isSuccess()) {
            startDuangContextListener();
            System.out.println("INFO: "+sdf.format(new Date())+" Server["+bootStrap.getHost()+":"+bootStrap.getPort()+"] startup in "+bootStrap.getStartTimeMillis()+" ms, God bless no bugs!");
        } else {
            System.out.println("INFO:  "+sdf.format(new Date())+" Server["+bootStrap.getHost()+":"+bootStrap.getPort()+"] startup failed");
        }
    }

    private void startDuangContextListener() {
        new ContextLoaderListener();
    }
}
