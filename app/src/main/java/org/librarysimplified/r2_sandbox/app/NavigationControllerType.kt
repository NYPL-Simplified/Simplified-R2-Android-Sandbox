package org.librarysimplified.r2_sandbox.app

/**
 * A mindlessly simple navigation controller.
 */

interface NavigationControllerType {

  /**
   * Open the reader.
   */

  fun openReader()

  /**
   * Pop whatever is onscreen now.
   */

  fun popBackStack()

}
