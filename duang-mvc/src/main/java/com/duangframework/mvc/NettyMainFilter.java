package com.duangframework.mvc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by laotang on 2017/10/30.
 */
public class NettyMainFilter {

    private static final Logger logger = LoggerFactory.getLogger(NettyMainFilter.class);

    public NettyMainFilter(ChannelHandlerContext context, FullHttpRequest request, FullHttpResponse response) {
        try {
            doFilter(context, request, response);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void doFilter(final ChannelHandlerContext context, final FullHttpRequest request, final FullHttpResponse response)  {
        response.setStatus(HttpResponseStatus.OK);
        byte[] body = request.content().array();
        String bodyString = Unpooled.copiedBuffer(body).toString();
        logger.warn(bodyString);
        ByteBuf buffer = Unpooled.copiedBuffer(body);
        response.content().writeBytes(buffer);
        context.writeAndFlush(response);

    }


}
