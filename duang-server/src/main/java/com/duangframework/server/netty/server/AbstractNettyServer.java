package com.duangframework.server.netty.server;

import com.duangframework.server.IServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author laotang
 * @date 2017/10/30
 */
public abstract class AbstractNettyServer implements IServer {

    private static Logger logger = LoggerFactory.getLogger(AbstractNettyServer.class);

    protected ServerBootstrap serverBootstrap;
    protected BootStrap bootStrap;

    public AbstractNettyServer(String host, int port) {
        bootStrap = new BootStrap(host, port);
        init();//初始化
    }

    private void init() {
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bootStrap.getBossGroup(), bootStrap.getWorkerGroup());
        serverBootstrap.option(ChannelOption.SO_BACKLOG, bootStrap.getBockLog())  //连接数
                .childOption(ChannelOption.ALLOCATOR, bootStrap.getAllocator())
                .childOption(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
                .childOption(ChannelOption.SO_RCVBUF, 65536)
                .childOption(ChannelOption.SO_SNDBUF, 65536)
                .childOption(ChannelOption.SO_REUSEADDR, true)//重用地址
                .childOption(ChannelOption.SO_KEEPALIVE, true)  //开启Keep-Alive，长连接
                .childOption(ChannelOption.TCP_NODELAY, true)  //不延迟，消息立即发送
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);
        serverBootstrap.channel(bootStrap.getDefaultChannel());
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
}
