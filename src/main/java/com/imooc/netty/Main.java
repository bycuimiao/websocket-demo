/*
 * Created by cuimiao on 2018/4/30.
 */

package com.imooc.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author cuimiao
 * @version 0.0.1
 * @Description:
 * @since 0.0.1 2018-04-30
 */
public class Main {
  public static void main(String[] args) {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap.group(bossGroup,workerGroup);
      serverBootstrap.channel(NioServerSocketChannel.class);
      serverBootstrap.childHandler(new MyWebSocketChannelHandler());
      System.out.print("服务器开启，等待客户端连接");
      Channel ch = serverBootstrap.bind(8888).sync().channel();
      ch.closeFuture().sync();
    }catch (Exception e){
      e.printStackTrace();
    }finally {
      //优雅的退出程序
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}