package org.librarysimplified.r2_sandbox.app

import org.readium.r2.shared.Publication
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
   * The URL used to access spine item `index` from a web view.
   */

  fun locationOfSpineItem(index: Int): String

  /**
   * The starting location used to access the book content from a web view
   */

  fun startingLocation(): String {
    return this.locationOfSpineItem(0)
  }

  /**
   * The loaded publication
   */

  val publication: Publication

}

