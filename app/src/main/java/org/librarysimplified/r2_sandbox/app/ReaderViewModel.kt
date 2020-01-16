package org.librarysimplified.r2_sandbox.app

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class ReaderViewModel : ViewModel() {

  private val logger =
    LoggerFactory.getLogger(ReaderViewModel::class.java)

  private val ioExecutor =
    MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1) { runnable ->
      val thread = Thread(runnable)
      thread.name = "org.librarysimplified.r2_sandbox.app.io"
      thread
    })

  val file: MutableLiveData<File?> =
    MutableLiveData()

  val server: MutableLiveData<ReaderServerType?> =
    MutableLiveData()

  var chapterIndex: MutableLiveData<Int> =
    MutableLiveData(0)

  override fun onCleared() {
    super.onCleared()

    this.ioExecutor.execute {
      this.closeExistingServer()
    }
  }

  private fun closeExistingServer() {
    val current = this.server.value
    current?.let { currentServer ->
      this.server.postValue(null)
      currentServer.close()
    }
  }

  fun openBook(
    context: Context,
    file: File
  ): ListenableFuture<ReaderServerType> {
    return this.ioExecutor.submit(Callable {
      this.closeExistingServer()
      val server = ReaderServer.create(context, file)
      this.server.postValue(server)
      server
    })
  }
}
