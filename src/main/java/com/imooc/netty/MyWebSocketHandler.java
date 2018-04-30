/*
 * Created by cuimiao on 2018/4/29.
 */

package com.imooc.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author cuimiao
 * @version 0.0.1
 * @Description: 核心业务处理类.
 * @since 0.0.1 2018-04-29
 */
public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

  private WebSocketServerHandshaker handshaker;

  private final static String WEB_SOCKET_URL = "ws://localhost:8888/websocket";

  //客户端与服务端创建连接的时候调用
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    //将channel放入group中.
    NettyConfig.group.add(ctx.channel());
    System.out.println("客户端与服务端连接开启...");
  }

  //客户端与服务端断开连接的时候调用
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    //移除group中的channel
    NettyConfig.group.remove(ctx.channel());
    System.out.println("客户端与服务端连接关闭...");
  }

  //服务端接受客户端发来的数据结束之后调用
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    //将缓冲区中的数据发送出去
    ctx.flush();
  }

  //功能出现异常的时候调用
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    //打印异常栈
    cause.printStackTrace();
    //关闭channel
    ctx.close();
  }

  //服务端处理客户端websocket请求的核心方法.
  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest ) {
      //处理客户端向服务端发起http握手请求的业务
      handHttpRequest(channelHandlerContext,(FullHttpRequest)msg);
    } else if (msg instanceof WebSocketFrame) {
      //处理WebSocket连接业务
      handWebsocketFrame(channelHandlerContext,(WebSocketFrame) msg);
    }
  }

  /**
   * 处理客户端向服务端发起http握手请求的业务.
   *
   * @param ctx
   * @param req
   */
  private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
    if (!req.getDecoderResult().isSuccess() || !("websocket".equals(req.headers().get("Upgrade")))) {
      //请求失败，或者不是websocket请求
      sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
      return;
    }
    WebSocketServerHandshakerFactory webSocketServerHandshakerFactory = new WebSocketServerHandshakerFactory(WEB_SOCKET_URL, null, false);
    handshaker = webSocketServerHandshakerFactory.newHandshaker(req);
    if(handshaker == null){
      WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
    }else {
      handshaker.handshake(ctx.channel(),req);
    }
  }

  /**
   * 处理客户端与服务端之间的websocket业务
   * @param ctx
   * @param frame
   */
  private void handWebsocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){
    //判断是否是关闭websocket的指令
    if(frame instanceof CloseWebSocketFrame){
      handshaker.close(ctx.channel(),(CloseWebSocketFrame) frame.retain());
    }
    //判断是否是ping消息
    if(frame instanceof PingWebSocketFrame){
      ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
      return;
    }
    //判断是否是二进制消息
    /*if(frame instanceof TextWebSocketFrame){
      System.err.println("暂不支持二进制消息");
      throw new RuntimeException("["+this.getClass().getName()+"]不支持二进制消息");
    }*/
    //返回应答消息
    //获取客户端向服务端发送的消息
    String request = ((TextWebSocketFrame)frame).text();
    System.out.println("服务端收到客户端消息===>>>"+request);
    TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString() + ctx.channel().id() + "===>>>" + request);
    //群发，服务端向每个客户端群发消息
    NettyConfig.group.writeAndFlush(tws);
  }

  /**
   * 服务端向客户端相应消息.
   *
   * @param ctx
   * @param req
   * @param res
   */
  private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
    if (res.getStatus().code() != 200) {
      //请求失败
      //获取到失败信息
      ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
      //写入response中
      res.content().writeBytes(buf);
      //释放缓存流
      buf.release();
    }
    //服务端向客户端发送数据
    ChannelFuture channelFuture = ctx.channel().writeAndFlush(res);
    if (res.getStatus().code() != 200) {
      //关闭channel
      channelFuture.addListener(ChannelFutureListener.CLOSE);
    }
  }
}