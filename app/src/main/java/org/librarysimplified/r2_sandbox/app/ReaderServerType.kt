package org.librarysimplified.r2_sandbox.app

import java.io.Closeable

/**
 * The interface exposed by the Readium server.
 */

interface ReaderServerType : Closeable {

  /**
   * The port number used to start the server.
   */

  val port: Int

  /**
   * The starting location used to access the book content from a web view
   */

  fun startingLocation(): String

}

