package org.librarysimplified.r2_sandbox.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity(), ToolbarHostType, NavigationControllerType {

  override fun openReader() {
    this.supportFragmentManager.beginTransaction()
      .replace(R.id.mainFragmentHost, ReaderViewerFragment(), "MAIN")
      .addToBackStack(null)
      .commit()
  }

  override fun popBackStack() {
    this.supportFragmentManager.popBackStack()
  }

  override fun findToolbar(): Toolbar {
    return this.findViewById(R.id.mainToolbar)
  }

  override fun onCreate(state: Bundle?) {
    super.onCreate(state)

    this.setContentView(R.layout.main_host)

    if (state == null) {
      val fragment = ReaderIntroFragment()
      this.supportFragmentManager.beginTransaction()
        .replace(R.id.mainFragmentHost, fragment, "MAIN")
        .commit()
    }
  }
}
