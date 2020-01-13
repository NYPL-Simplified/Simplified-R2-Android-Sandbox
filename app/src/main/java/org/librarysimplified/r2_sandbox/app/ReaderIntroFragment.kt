package org.librarysimplified.r2_sandbox.app

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.zaphlabs.filechooser.KnotFileChooser
import com.zaphlabs.filechooser.Sorter

/**
 * A fragment that allows for selecting a book from the device filesystem.
 */

class ReaderIntroFragment : Fragment() {

  private lateinit var choose: Button
  private lateinit var path: TextView
  private lateinit var read: Button
  private lateinit var readerModel: ReaderViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val layout =
      inflater.inflate(R.layout.reader_intro, container, false)

    this.path =
      layout.findViewById(R.id.fileChooserPath)
    this.choose =
      layout.findViewById(R.id.fileChooseButton)
    this.read =
      layout.findViewById(R.id.fileReadButton)

    this.read.isEnabled = false
    return layout
  }

  override fun onStart() {
    super.onStart()

    val toolbarHost = (this.requireActivity() as ToolbarHostType)
    toolbarHost.toolbarClearMenu()
    toolbarHost.toolbarSetTitleSubtitle("R2-Sandbox (Chooser)", "")

    this.readerModel =
      ViewModelProviders.of(this.requireActivity())
        .get(ReaderViewModel::class.java)

    this.readerModel.file.observe(this, Observer { file ->
      if (file != null) {
        this.path.text = file.absolutePath
        this.read.isEnabled = true
      } else {
        this.path.text = getString(R.string.fileNoneSelected)
        this.read.isEnabled = false
      }
    })

    this.choose.setOnClickListener {
      KnotFileChooser(
        context = this.requireContext(),
        allowBrowsing = true,
        allowCreateFolder = false,
        allowMultipleFiles = false,
        allowSelectFolder = false,
        minSelectedFiles = 1,
        maxSelectedFiles = 1,
        showFiles = true,
        showFoldersFirst = true,
        showFolders = true,
        showHiddenFiles = true,
        initialFolder = Environment.getExternalStorageDirectory(),
        restoreFolder = false,
        cancelable = true
      )
        .title(R.string.fileSelectTitle)
        .sorter(Sorter.ByNameInAscendingOrder)
        .onSelectedFilesListener { selectedFiles ->
          if (selectedFiles.isNotEmpty()) {
            this.readerModel.file.value = selectedFiles[0]
          } else {
            this.readerModel.file.value = null
          }
        }
        .show()
    }

    this.read.setOnClickListener {
      (this.requireActivity() as NavigationControllerType).openReader()
    }
  }
}