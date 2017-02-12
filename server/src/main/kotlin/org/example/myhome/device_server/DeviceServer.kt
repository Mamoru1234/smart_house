package org.example.myhome.device_server

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.logging.LoggingHandler
import org.example.myhome.device_server.handlers.DeviceRegistration
import org.example.myhome.server.startNettyServer
import org.example.myhome.services.DeviceRegisterService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import kotlin.reflect.jvm.internal.impl.javax.inject.Inject


@Component
open class DeviceServer (
  @Inject
  @Qualifier("bossGroup")
  val bossGroup:NioEventLoopGroup,

  @Inject
  @Qualifier("workerGroup")
  val workerGroup:NioEventLoopGroup,

  val deviceRegisterService: DeviceRegisterService
) {
  open fun start(): ChannelFuture? {
    val init =object: ChannelInitializer<SocketChannel>(){
      override fun initChannel(ch: SocketChannel?) {
        ch?.pipeline()?.addLast(
          LengthFieldBasedFrameDecoder(Int.MAX_VALUE, 0, 4, 0, 4),
          StringDecoder(),
          LengthFieldPrepender(4),
          StringEncoder(),
          DeviceRegistration(deviceRegisterService),
          LoggingHandler()
        )
      }
    }
    return startNettyServer(7080, bossGroup, workerGroup, init)
  }
}