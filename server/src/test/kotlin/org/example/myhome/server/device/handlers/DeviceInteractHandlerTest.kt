package org.example.myhome.server.device.handlers

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.netty.channel.ChannelHandlerContext
import org.example.myhome.simp.core.SimpMessage
import org.example.myhome.simp.core.SimpMessageType
import org.example.myhome.utils.readTree
import org.example.myhome.utils.writeValue
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.testng.annotations.Test
import reactor.test.StepVerifier
import java.util.*

class DeviceInteractHandlerTest {
  @Test(timeOut = 10000)
  fun subscribe() {
    val handler = DeviceInteractHandler()
    val context = Mockito.mock(ChannelHandlerContext::class.java)
    val message = mapOf(
      "topic" to "test",
      "body" to UUID.randomUUID().toString()
    )
    handler.channelRegistered(context)
    Mockito.verify(context).fireChannelRegistered()
    StepVerifier.create(handler.subscribe("test").take(1))
      .then {
        Mockito.verify(context).writeAndFlush(SimpMessage(
          type = SimpMessageType.SUBSCRIBE,
          body = "{\"topic\":\"test\"}"
        ))
        handler.channelRead(context, SimpMessage(type = SimpMessageType.MESSAGE, body = writeValue(message)))
      }
      .expectNext(message["body"])
      .then {
        Mockito.verifyNoMoreInteractions(context)
      }
      .verifyComplete()
  }

  @Test(timeOut = 10000)
  fun send() {
    val handler = DeviceInteractHandler()
    val captor = ArgumentCaptor.forClass(SimpMessage::class.java)
    val context = Mockito.mock(ChannelHandlerContext::class.java)
    handler.channelRegistered(context)
    val body = UUID.randomUUID().toString()
    val requestBody = UUID.randomUUID().toString()
    StepVerifier.create(handler.send("test", requestBody))
      .then {
        Mockito.verify(context).writeAndFlush(captor.capture())
        val config = readTree(captor.value.body)
        assertThat(config.hasNonNull("id"), equalTo(true))
        val message = mapOf(
          "id" to config["id"].asInt(),
          "body" to body
        )
        assertThat(config["body"].asText(), equalTo(requestBody))
        handler.channelRead(context, SimpMessage(type = SimpMessageType.RESPONSE, body = writeValue(message)))
      }
      .expectNext(body)
      .verifyComplete()
  }

}
