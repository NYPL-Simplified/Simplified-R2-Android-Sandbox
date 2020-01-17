package org.librarysimplified.r2_sandbox.app

import android.webkit.WebView
import org.slf4j.LoggerFactory

/**
 * The default implementation of the reader javascript API.
 */

class ReaderJavascriptAPI(
  private val webView: WebView
) : ReaderJavascriptAPIType {

  private val logger =
    LoggerFactory.getLogger(ReaderJavascriptAPI::class.java)

  override fun scrollNext() {
    UIThread.checkIsUIThread()

    this.webView.evaluateJavascript("scrollRight();") { value ->
      this.logger.debug("evaluation result: {}", value)
    }
  }

  override fun scrollPrevious() {
    UIThread.checkIsUIThread()

    this.webView.evaluateJavascript("scrollLeft();") { value ->
      this.logger.debug("evaluation result: {}", value)
    }
  }
}
