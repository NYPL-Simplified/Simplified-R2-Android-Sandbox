package org.librarysimplified.r2_sandbox.app

import org.readium.r2.streamer.parser.EpubParser
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

object ReaderController {

  private val logger =
    LoggerFactory.getLogger(ReaderController::class.java)

  fun openBook(file: File) {
    val box =
      EpubParser().parse(file.absolutePath)
        ?: throw IOException("Failed to parse EPUB")

    this.logger.debug("publication uri: {}", box.publication.baseUrl())
    this.logger.debug("publication title: {}", box.publication.metadata.title)
  }
}
