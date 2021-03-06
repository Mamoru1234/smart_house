package org.example.myhome

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.benas.randombeans.EnhancedRandomBuilder
import io.github.benas.randombeans.api.EnhancedRandom
import org.example.myhome.dto.DeviceMetaDataDto
import org.example.myhome.simp.core.SimpMessageType
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.*

//TODO move it to separate module with integration tests
class ServerSpec {

  lateinit var socket: Socket
  lateinit var inputStream: InputStream
  lateinit var outputStream: OutputStream

  @BeforeMethod
  fun setUp() {
    socket = Socket("localhost", 7080)
    inputStream = socket.inputStream
    outputStream = socket.outputStream
  }

  @AfterMethod
  fun tearDown() {
    socket.close()
  }

  @Test(timeOut = 10000)
  fun testRegistration() {
    val deviceMetaData = DeviceMetaDataDto(
      deviceId = UUID.randomUUID().toString(),
      deviceKey = UUID.randomUUID().toString(),
      firmwareVersion = 0,
      sensors = listOf(1, 2, 3),
      controls = listOf(0, 1, 3)
    )
    outputStream.writeJson(deviceMetaData, SimpMessageType.REQUEST)
    val response = inputStream.readSimpMessage()
    assertThat(response.type, equalTo(SimpMessageType.RESPONSE))
    assertThat(response.body, equalTo("NO"))
//    assert that client is disconnected
    assertThat(inputStream.read(), equalTo(-1))
  }

  @Test(timeOut = 10000)
  fun testInitialFrameLimit() {
    val random = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
      .stringLengthRange(255, 255)
      .build()
    val message = random.nextObject(String::class.java)
    outputStream.writeString(message, SimpMessageType.REQUEST)
//    assert that client is disconnected
    assertThat(inputStream.read(), equalTo(-1))
  }
}
