package org.librarysimplified.r2_sandbox.app

import android.webkit.WebView
import androidx.annotation.UiThread
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

    this.webView.evaluateJavascript("scrollRight();") {
      this.logger.debug("evaluation result: {}", it)
    }
  }

  override fun scrollPrevious() {
    UIThread.checkIsUIThread()

    this.webView.evaluateJavascript("scrollLeft();") {
      this.logger.debug("evaluation result: {}", it)
    }
  }

  override fun setFontFamily(value: String) {
    setUserProperty("fontFamily", value)
    setUserProperty("fontOverride", "readium-font-on")
  }

  override fun setTextSize(value: Int) {
    // Note: The js property name is 'fontSize' not 'textSize'
    setUserProperty("fontSize", "${value}%")
  }

  override fun setTextAlign(value: String) {
    setUserProperty("textAlign", value)
  }

  override fun setPageMargin(value: Double) {
    // Note: The js property name is 'pageMargins' plural
    setUserProperty("pageMargins", "$value")
  }

  override fun setLineHeight(value: Double) {
    setUserProperty("lineHeight", "$value")
  }

  override fun setLetterSpacing(value: Double) {
    setUserProperty("letterSpacing", "${value}em")
  }

  override fun setWordSpacing(value: Double) {
    setUserProperty("wordSpacing", "${value}rem")
  }

  @UiThread
  fun setUserProperty(name: String, value: String) {
    UIThread.checkIsUIThread()

    val script = "setProperty(\"--USER__${name}\", \"${value}\");"
    this.webView.evaluateJavascript(script) {
      this.logger.debug("evaluation result: {}", it)
    }
  }
}
