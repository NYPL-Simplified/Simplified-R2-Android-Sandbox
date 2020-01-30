package org.librarysimplified.r2_sandbox.app

import androidx.annotation.UiThread

/**
 * The javascript API exposed by the reader.
 */

interface ReaderJavascriptAPIType {

  /**
   * Scroll to the next page.
   */

  @UiThread
  fun scrollNext()

  /**
   * Scroll to the previous page.
   */

  @UiThread
  fun scrollPrevious()

  /**
   * Set the font family.
   */

  @UiThread
  fun setFontFamily(value: String)

  /**
   * Set the text size.
   */

  @UiThread
  fun setTextSize(value: Int)

  /**
   * Set the text alignment.
   */

  @UiThread
  fun setTextAlign(value: String)
}