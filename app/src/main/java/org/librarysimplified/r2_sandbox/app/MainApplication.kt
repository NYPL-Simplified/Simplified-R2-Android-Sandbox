package org.librarysimplified.r2_sandbox.app

import androidx.multidex.MultiDexApplication
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.rolling.RollingFileAppender
import org.slf4j.LoggerFactory
import java.io.File

class MainApplication : MultiDexApplication() {

  companion object {
    private lateinit var INSTANCE: MainApplication

    @JvmStatic
    val application: MainApplication
      get() = this.INSTANCE
  }

  private val logger = LoggerFactory.getLogger(MainApplication::class.java)

  override fun onCreate() {
    super.onCreate()

    this.configureLogging()
    this.logger.debug("starting app: pid {}", android.os.Process.myPid())
    INSTANCE = this
  }

  /**
   * We apparently can't rely on the paths configured in logback.xml to actually work
   * correctly across different devices. This bit of code tries to configure the path
   * of the log file directly.
   */

  private fun configureLogging() {
    try {
      val context = LoggerFactory.getILoggerFactory() as LoggerContext
      for (logger in context.loggerList) {
        val index = logger.iteratorForAppenders()
        while (index.hasNext()) {
          val appender = index.next()
          if (appender is RollingFileAppender<*>) {
            externalCacheDir?.mkdirs()
            val path = File(externalCacheDir, "log.txt").absolutePath
            (appender as RollingFileAppender<*>).file = path
            appender.start()
          }
        }
      }
      this.logger.debug("logging is configured")
    } catch (e: Exception) {
      this.logger.error("could not configure logging: ", e)
    }
  }
}
