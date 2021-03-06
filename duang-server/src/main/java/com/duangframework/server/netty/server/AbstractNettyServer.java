package com.duangframework.server.netty.server;

import com.duangframework.core.exceptions.MvcStartUpException;
import com.duangframework.core.interfaces.IContextLoaderListener;
import com.duangframework.core.interfaces.IProcess;
import com.duangframework.core.kit.ToolsKit;
import com.duangframework.core.utils.OS;
import com.duangframework.server.IServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.util.ResourceLeakDetector;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.charset.Charset;

/**
 *
 * @author laotang
 * @date 2017/10/30
 */
public abstract class AbstractNettyServer implements IServer {

    private static Logger logger = LoggerFactory.getLogger(AbstractNettyServer.class);

    protected ServerBootstrap nettyBootstrap;
    protected BootStrap bootStrap;

    public AbstractNettyServer(String host, int port) {
        bootStrap = new BootStrap(host, port);
        init();//初始化
    }

    public AbstractNettyServer(String host, int port, boolean isDebug, IContextLoaderListener listener, IProcess mainProcess) {
        bootStrap = new BootStrap(host, port);
        bootStrap.setStartContextListener(listener);
        bootStrap.setMainProcess(mainProcess);
        bootStrap.setDebug(isDebug);
        init();//初始化
    }

    private void init() {
        if(bootStrap.getPort()== 0){
            throw new MvcStartUpException("Server Startup Fail: " + bootStrap.getPort() + " is not setting!");
        }
        if(isUse()){
            throw new MvcStartUpException("Server Startup Fail: " + bootStrap.getPort() + " is use!");
        }
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        nettyBootstrap = new ServerBootstrap();
        nettyBootstrap.group(bootStrap.getBossGroup(), bootStrap.getWorkerGroup());
        nettyBootstrap.option(ChannelOption.SO_BACKLOG, bootStrap.getBockLog())  //连接数
                .childOption(ChannelOption.ALLOCATOR, bootStrap.getAllocator())
                .childOption(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
                .childOption(ChannelOption.SO_RCVBUF, 65536)
                .childOption(ChannelOption.SO_SNDBUF, 65536)
                .childOption(ChannelOption.SO_REUSEADDR, true)//重用地址
                .childOption(ChannelOption.SO_KEEPALIVE, true)  //开启Keep-Alive，长连接
                .childOption(ChannelOption.TCP_NODELAY, true)  //不延迟，消息立即发送
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);
        nettyBootstrap.channel(bootStrap.getDefaultChannel());
    }

    @Override
    public abstract void start();

    @Override
    public void shutdown() {
        try {
            bootStrap.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown();
            }
        });
    }

    /**
     * 检测本机指定端口是否可用<br/>
     * 如果端口可用，则返回false
     * @return
     */
    private boolean isUse() {
//        if(port < minPort || port > maxPort) throw new IllegalStateException("port only range is "+minPort+"~"+maxPort);
        ServerSocket ss  = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(bootStrap.getPort());
            ss.setReuseAddress(true);
            ds = new DatagramSocket(bootStrap.getPort());
            ds.setReuseAddress(true);
            logger.warn(ss.getInetAddress().getHostName()+":"+ ss.getLocalPort() +" is not use!");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            try {
                if(ToolsKit.isNotEmpty(ds)) {
                    ds.close();
                }
                if(ToolsKit.isNotEmpty(ss)) {
                    ss.close();
                }
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return true;
    }

    private String pidFile() {
        String pidFile = System.getProperty("pidfile");
        if (ToolsKit.isEmpty(pidFile)) {
            pidFile = "duang.pid";
        }
        return pidFile;
    }

    public void writePidFile() {
        String pidFile = pidFile();
        OS os = OS.get();
        String pid= "";
        if (os.isLinux()) {
            File proc_self = new File("/proc/self");
            if(proc_self.exists()) {
                try {
                    pid = proc_self.getCanonicalFile().getName();
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
            File bash = new File("/bin/sh");
            if(bash.exists()) {
                ProcessBuilder pb = new ProcessBuilder("/bin/sh","-c","echo $PPID");
                try {
                    Process p = pb.start();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    pid = rd.readLine();
                } catch(IOException e) {
                    pid = String.valueOf(Thread.currentThread().getId());
                }
            }
        } else {
            try {
                // see http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id
                String name = ManagementFactory.getRuntimeMXBean().getName();
                int pos = name.indexOf('@');
                if (pos > 0) {
                    pid = name.substring(0, pos);
                } else {
                    logger.warn("Write pid file not supported on non-linux system");
                    return;
                }
            } catch (Exception e) {
                logger.warn("Write pid file not supported on non-linux system");
                return;
            }
        }
        try {
            clearPidFile();
            FileUtils.writeStringToFile(new File(pidFile), pid, Charset.forName("UTF-8"));
//            Runtime.getRuntime().addShutdownHook(new Thread() {
//                @Override
//                public void run() {
//                    clearPidFile();
//                }
//            });
        } catch (Exception e) {
            logger.warn("Error writing pid file: %s", e.getMessage(), e);
        }
    }

    public void clearPidFile() {
        String pidFile = pidFile();
        try {
            File file = new File(pidFile);
            if (!file.delete()) {
                file.deleteOnExit();
            }
        } catch (Exception e) {
            logger.warn("Error delete pid file: %s", pidFile, e);
        }
    }
}
