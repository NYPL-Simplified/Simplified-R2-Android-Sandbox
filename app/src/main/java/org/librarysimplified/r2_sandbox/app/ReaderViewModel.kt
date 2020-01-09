package org.librarysimplified.r2_sandbox.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.util.concurrent.MoreExecutors
import java.io.File
import java.util.concurrent.Executors

class ReaderViewModel : ViewModel() {

  val ioExecutor =
    MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1) { runnable ->
      val thread = Thread(runnable)
      thread.name = "org.librarysimplified.r2_sandbox.app.io"
      thread
    })

  val file: MutableLiveData<File?> =
    MutableLiveData()

}
