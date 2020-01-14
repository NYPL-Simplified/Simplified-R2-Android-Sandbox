package org.librarysimplified.r2_sandbox.app

import android.content.Context
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.container.Container
import org.readium.r2.streamer.parser.EpubParser
import org.readium.r2.streamer.server.Server
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The default Readium server implementation.
 */

class ReaderServer private constructor(
  override val port: Int,
  private val server: Server,
  private val publication: Publication,
  private val container: Container,
  private val epubFileName: String
) : ReaderServerType {

  private val logger =
    LoggerFactory.getLogger(ReaderServer::class.java)

  private val running = AtomicBoolean(true)

  override fun close() {
    if (this.running.compareAndSet(true, false)) {
      this.logger.debug("shutting down server")
      this.server.closeAllConnections()
      this.server.stop()
    }
  }

  companion object {

    private val logger =
      LoggerFactory.getLogger(ReaderServer::class.java)

    /**
     * Find a high-numbered port upon which to run the internal server. Tries up to ten
     * times to find a port and then gives up with an exception if it can't.
     */

    private fun fetchUnusedHTTPPort(): Int {
      for (i in 0 until 10) {
        try {
          val socket = ServerSocket(0)
          val port = socket.localPort
          socket.close()
          return port
        } catch (e: IOException) {
          this.logger.error("failed to open port: ", e)
        }

        try {
          Thread.sleep(1_000L)
        } catch (e: InterruptedException) {
          Thread.currentThread().interrupt()
        }
      }

      throw IOException("Unable to find an unused port for the server")
    }

    /**
     * Create a new server hosting the given EPUB file.
     *
     * @throws IOException On any errors
     */

    @Throws(IOException::class)
    fun create(
      context: Context,
      file: File
    ): ReaderServerType {
      val box =
        EpubParser().parse(file.absolutePath)
          ?: throw IOException("Failed to parse EPUB")

      this.logger.debug("publication uri: {}", box.publication.baseUrl())
      this.logger.debug("publication title: {}", box.publication.metadata.title)
      val port = this.fetchUnusedHTTPPort()
      this.logger.debug("server port: {}", port)

      val server = Server(port)
      this.logger.debug("starting server")
      server.start(5_000)

      this.logger.debug("loading readium resources")
      server.loadReadiumCSSResources(context.assets)
      server.loadR2ScriptResources(context.assets)

      this.logger.debug("loading epub into server")
      val epubName = "/${file.name}"
      server.addEpub(
        publication = box.publication,
        container = box.container,
        fileName = epubName,
        userPropertiesPath = null
      )

      this.logger.debug("server ready")
      return ReaderServer(
        port = port,
        server = server,
        epubFileName = epubName,
        publication = box.publication,
        container = box.container
      )
    }
  }

  override fun startingLocation(): String {
    return buildString {
      this.append("http://127.0.0.1:")
      this.append(this@ReaderServer.port)
      this.append(this@ReaderServer.epubFileName)

      val publication = this@ReaderServer.publication
      val firstItem = publication.readingOrder.firstOrNull()?.href
      if (firstItem != null) {
        this.append(firstItem)
      }
    }
  }
}