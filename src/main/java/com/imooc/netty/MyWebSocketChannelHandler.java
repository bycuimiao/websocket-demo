/*
 * Created by cuimiao on 2018/4/30.
 */

package com.imooc.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author cuimiao
 * @version 0.0.1
 * @Description: 初始化连接时候的各个组件
 * @since 0.0.1 2018-04-30
 */
public class MyWebSocketChannelHandler extends ChannelInitializer<SocketChannel> {
  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    socketChannel.pipeline().addLast("http-codec",new HttpServerCodec());
    socketChannel.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
    socketChannel.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
    socketChannel.pipeline().addLast("handler",new MyWebSocketHandler());
  }
}