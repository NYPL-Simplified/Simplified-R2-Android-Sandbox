package org.librarysimplified.r2_sandbox.app

/**
 * Methods called from javascript running inside a WebView.
 */

interface ReaderJavascriptAPIReceiverType {

  /**
   * The progression of the reader within a chapter has changed.
   */

  @android.webkit.JavascriptInterface
  fun onChapterProgressionChanged(positionString: String)

  /**
   * The center of the screen was tapped.
   */

  @android.webkit.JavascriptInterface
  fun onCenterTapped()

  /**
   * The screen was clicked somewhere.
   */

  @android.webkit.JavascriptInterface
  fun onClicked()

  /**
   * The left edge of the screen was tapped.
   */

  @android.webkit.JavascriptInterface
  fun onLeftTapped()

  /**
   * The right edge of the screen was tapped.
   */

  @android.webkit.JavascriptInterface
  fun onRightTapped()
}