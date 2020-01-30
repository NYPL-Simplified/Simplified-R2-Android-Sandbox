package org.librarysimplified.r2_sandbox.app

import androidx.annotation.UiThread

/**
 * The javascript API exposed by the reader.
 */

interface ReaderJavascriptAPIType {

  /** Scroll to the next page. */

  @UiThread
  fun scrollNext()

  /** Scroll to the previous page. */

  @UiThread
  fun scrollPrevious()

  @UiThread
  fun setFontFamily(value: String)

  @UiThread
  fun setTextSize(value: Int)

  @UiThread
  fun setTextAlign(value: String)

  @UiThread
  fun setPageMargin(value: Double)

  @UiThread
  fun setLineHeight(value: Double)

  @UiThread
  fun setLetterSpacing(value: Double)

  @UiThread
  fun setWordSpacing(value: Double)
}