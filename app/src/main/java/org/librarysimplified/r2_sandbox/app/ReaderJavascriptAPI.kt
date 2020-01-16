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

  init {

    /*
     * The scripts included in Readium 2 require the ability to make calls back
     * to the host application via an `Android` object visible to the javascript.
     * We register that object here: See all the methods in this class annotated
     * with [@android.webkit.JavascriptInterface] to see which methods will be
     * called from javascript.
     */

    this.webView.addJavascriptInterface(this, "Android")
  }

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

  /**
   * Called when the position of the reader within a chapter has changed.
   */

  @android.webkit.JavascriptInterface
  fun progressionDidChange(positionString: String) {
    this.logger.debug("progressionDidChange: {}", positionString)
  }
}
