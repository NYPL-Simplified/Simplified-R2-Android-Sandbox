package org.librarysimplified.r2_sandbox.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.common.base.Function
import com.google.common.util.concurrent.FluentFuture
import com.google.common.util.concurrent.MoreExecutors
import org.readium.r2.navigator.R2WebView
import org.slf4j.LoggerFactory

/**
 * A fragment that loads Readium and tries to display a book.
 */

class ReaderViewerFragment : Fragment() {

  private val logger =
    LoggerFactory.getLogger(ReaderViewerFragment::class.java)

  private lateinit var readerModel: ReaderViewModel
  private lateinit var webView: R2WebView
  private lateinit var webViewClient: ReaderWebViewClient
  private lateinit var webChromeClient: ReaderWebChromeClient

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val layout =
      inflater.inflate(R.layout.reader_viewer, container, false)

    this.webViewClient = ReaderWebViewClient()
    this.webChromeClient = ReaderWebChromeClient()

    this.webView = layout.findViewById(R.id.viewerWebView)
    this.webView.webViewClient = this.webViewClient
    this.webView.webChromeClient = this.webChromeClient
    return layout
  }

  override fun onStart() {
    super.onStart()

    val activity = this.requireActivity()
    val toolbarHost = (activity as ToolbarHostType)
    toolbarHost.toolbarClearMenu()
    toolbarHost.toolbarSetTitleSubtitle("R2-Sandbox (Viewer)", "")

    this.readerModel =
      ViewModelProviders.of(activity)
        .get(ReaderViewModel::class.java)

    val bookFile = this.readerModel.file.value
    if (bookFile == null) {
      this.close()
      return
    }

    val exec = MoreExecutors.directExecutor()
    FluentFuture.from(this.readerModel.openBook(activity, bookFile))
      .transform(Function<ReaderServerType, Unit> { server ->
        this.bookIsReady(server!!)
      }, exec)
      .catching(Exception::class.java, Function<java.lang.Exception, Unit> { exception ->
        this.bookFailedToOpen(exception!!)
      }, exec)
  }

  private fun bookFailedToOpen(exception: java.lang.Exception) {
    this.logger.error("failed to open book: ", exception)

    UIThread.runOnUIThread {
      AlertDialog.Builder(this.requireContext())
        .setMessage(exception.message)
        .setOnDismissListener {
          this.close()
        }.show()
    }
  }

  private fun bookIsReady(server: ReaderServerType) {
    UIThread.runOnUIThread {
      this.bookIsReadyUI(server)
    }
  }

  @UiThread
  private fun bookIsReadyUI(server: ReaderServerType) {
    val startingLocation = server.startingLocation()
    this.logger.debug("opening starting location: {}", startingLocation)
    this.webView.loadUrl(startingLocation)
  }

  private fun close() {
    (this.requireActivity() as NavigationControllerType).popBackStack()
  }
}
