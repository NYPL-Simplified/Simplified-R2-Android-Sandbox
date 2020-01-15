package org.librarysimplified.r2_sandbox.app

import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import org.slf4j.LoggerFactory

class ReaderWebViewClient : WebViewClient() {

  companion object {
    val emptyResponse = WebResourceResponse(
        "text/plain",
        "utf-8",
        404,
        "Not Found",
        null,
        null
    )
  }

  private val logger =
    LoggerFactory.getLogger(ReaderWebViewClient::class.java)

  override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
    if (request != null) {
      val url = request.url.toString()

      if (url.endsWith("favicon.ico")) {
        return emptyResponse
      }
    }
    return super.shouldInterceptRequest(view, request)
  }

  override fun onLoadResource(
    view: WebView,
    url: String
  ) {
    this.logger.debug("onLoadResource: {}", url)
    super.onLoadResource(view, url)
  }

  override fun onReceivedError(
    view: WebView,
    request: WebResourceRequest,
    error: WebResourceError
  ) {
    if (Build.VERSION.SDK_INT >= 23) {
      this.logger.error(
        "onReceivedError: {}: {} {}",
        request.url,
        error.errorCode,
        error.description
      )
    }
    super.onReceivedError(view, request, error)
  }

  override fun onReceivedHttpError(
    view: WebView,
    request: WebResourceRequest,
    errorResponse: WebResourceResponse
  ) {
    this.logger.error(
      "onReceivedHttpError: {}: {} {}",
      request.url,
      errorResponse.statusCode,
      errorResponse.reasonPhrase
    )
    super.onReceivedHttpError(view, request, errorResponse)
  }

  override fun onReceivedError(
    view: WebView,
    errorCode: Int,
    description: String,
    failingUrl: String
  ) {
    this.logger.error(
      "onReceivedError: {}: {} {}",
      failingUrl,
      errorCode,
      description
    )
    super.onReceivedError(view, errorCode, description, failingUrl)
  }
}
