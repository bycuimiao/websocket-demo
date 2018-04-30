/*
 * Created by cuimiao on 2018/4/29.
 */

package com.imooc.netty;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author cuimiao
 * @version 0.0.1
 * @Description: 存储工程的全局变量.
 * @since 0.0.1 2018-04-29
 */
public class NettyConfig {
  public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}