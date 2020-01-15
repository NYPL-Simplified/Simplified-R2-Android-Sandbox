package org.librarysimplified.r2_sandbox.app.tests

import org.junit.Assert
import org.junit.Test
import org.readium.r2.streamer.parser.EpubParser
import org.readium.r2.streamer.server.Server
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL

class ServerTest {

  @Test
  fun testServer() {
    val file =
      this.copyResource("charles-dickens_great-expectations.epub")

    val pubBox =
      EpubParser().parse(file.absolutePath)
        ?: throw IOException("Failed to parse epub ${file}")

    val server = Server(8080)
    server.start(5_000)
    server.addEpub(pubBox.publication, pubBox.container, "/${file.name}", null)

    val targetURL = URL("http://127.0.0.1:8080/epub/text/chapter-1.xhtml")
    val bytes = targetURL.openStream().readBytes()
    Assert.assertTrue(bytes.isNotEmpty())
  }

  private fun copyResource(name: String): File {
    val url =
      ServerTest::class.java.getResource(
        "/org/librarysimplified/r2_sandbox/app/tests/${name}"
      ) ?: throw FileNotFoundException(name)

    return url.openStream().use { inputStream ->
      val file = File.createTempFile("/tmp", ".epub")
      file.outputStream().use { outputStream ->
        inputStream.copyTo(outputStream)
        file
      }
    }
  }
}