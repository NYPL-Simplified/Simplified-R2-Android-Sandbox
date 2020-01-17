package org.librarysimplified.r2_sandbox.app

import android.webkit.WebView
import org.slf4j.LoggerFactory

class ReaderJavascriptAPIReceiver(
  private val webView: WebView
) : ReaderJavascriptAPIReceiverType {

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

  private val logger =
    LoggerFactory.getLogger(ReaderJavascriptAPI::class.java)

  @android.webkit.JavascriptInterface
  override fun onChapterProgressionChanged(positionString: String) {
    this.logger.debug("onChapterProgressionChanged: {}", positionString)
  }

  @android.webkit.JavascriptInterface
  override fun onCenterTapped() {
    this.logger.debug("onCenterTapped")
  }

  @android.webkit.JavascriptInterface
  override fun onClicked() {
    this.logger.debug("onClicked")
  }

  @android.webkit.JavascriptInterface
  override fun onLeftTapped() {
    this.logger.debug("onLeftTapped")
  }

  @android.webkit.JavascriptInterface
  override fun onRightTapped() {
    this.logger.debug("onRightTapped")
  }
}