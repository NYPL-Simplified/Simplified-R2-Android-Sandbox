package org.librarysimplified.r2_sandbox.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.io.FileOutputStream


/**
 * A fragment that allows for selecting a book from the device filesystem.
 */

class ReaderIntroFragment : Fragment() {

  private lateinit var choose: Button
  private lateinit var path: TextView
  private lateinit var read: Button
  private lateinit var readerModel: ReaderViewModel

  companion object {
    const val PICK_BOOK = 1
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      PICK_BOOK -> onPickBookResult(resultCode, data)
    }
  }

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


    val context = requireContext()
    val epubFile = File(context.filesDir, "epub")
    val inputStream = context.assets.open("Temple.epub")

    // ...
    FileOutputStream(epubFile).use {
      inputStream.copyTo(it)
    }
    this.readerModel.file.value = epubFile

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
      val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "application/epub+zip"
        addCategory(Intent.CATEGORY_OPENABLE)
        putExtra(Intent.EXTRA_LOCAL_ONLY, true)
      }
      val chooser = Intent.createChooser(intent, getString(R.string.fileSelectTitle))
      startActivityForResult(chooser, PICK_BOOK)
    }

    this.read.setOnClickListener {
      (this.requireActivity() as NavigationControllerType).openReader()
    }
  }

  private fun onPickBookResult(resultCode: Int, data: Intent?) {
    Log.d("Tristan", data.toString())
    when (resultCode) {
      Activity.RESULT_OK -> {
        this.readerModel.file.value = data?.data?.let { File(it.path) }
      }
    }
  }
}