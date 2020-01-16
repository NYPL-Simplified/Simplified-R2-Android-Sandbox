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
}