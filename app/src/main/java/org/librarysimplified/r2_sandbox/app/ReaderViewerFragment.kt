package org.librarysimplified.r2_sandbox.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import org.readium.r2.navigator.R2WebView
import org.slf4j.LoggerFactory

class ReaderViewerFragment : Fragment() {

  private val logger =
    LoggerFactory.getLogger(ReaderViewerFragment::class.java)

  private lateinit var readerModel: ReaderViewModel
  private lateinit var webView: R2WebView

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val layout =
      inflater.inflate(R.layout.reader_viewer, container, false)

    this.webView =
      layout.findViewById(R.id.viewerWebView)

    return layout
  }

  override fun onStart() {
    super.onStart()

    val toolbarHost = (this.requireActivity() as ToolbarHostType)
    toolbarHost.toolbarClearMenu()
    toolbarHost.toolbarSetTitleSubtitle("R2-Sandbox (Viewer)", "")

    this.readerModel =
      ViewModelProviders.of(this.requireActivity())
        .get(ReaderViewModel::class.java)

    val bookFile = this.readerModel.file.value
    if (bookFile == null) {
      close()
      return
    }

    this.readerModel.ioExecutor.execute {
      try {
        this.logger.debug("opening book: {}", bookFile)
        ReaderController.openBook(bookFile)
        this.logger.debug("opened book")
      } catch (e: Exception) {
        this.logger.error("failed to open book: ", e)

        UIThread.runOnUIThread {
          AlertDialog.Builder(this.requireContext())
            .setMessage(e.message)
            .setOnDismissListener {
              this.close()
            }.show()
        }
      }
    }
  }

  private fun close() {
    (this.requireActivity() as NavigationControllerType).popBackStack()
  }
}
