package org.librarysimplified.r2_sandbox.app

import android.webkit.WebChromeClient
import org.slf4j.LoggerFactory

class ReaderWebChromeClient : WebChromeClient() {

  private val logger =
    LoggerFactory.getLogger(ReaderWebChromeClient::class.java)

  override fun onConsoleMessage(
    message: String,
    lineNumber: Int,
    sourceID: String
  ) {
    this.logger.debug("onConsoleMessage: {}:{}: {}", sourceID, lineNumber, message)
    super.onConsoleMessage(message, lineNumber, sourceID)
  }
}
