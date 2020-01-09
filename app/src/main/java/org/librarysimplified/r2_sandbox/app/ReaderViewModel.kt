package org.librarysimplified.r2_sandbox.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class ReaderViewModel : ViewModel() {

  val file: MutableLiveData<File?> =
    MutableLiveData()

}
