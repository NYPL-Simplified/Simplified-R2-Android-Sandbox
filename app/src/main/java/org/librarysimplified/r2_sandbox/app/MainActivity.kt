package org.librarysimplified.r2_sandbox.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity(), ToolbarHostType {

  private lateinit var toolbar: Toolbar

  override fun findToolbar(): Toolbar {
    return this.findViewById(R.id.mainToolbar)
  }

  override fun onCreate(state: Bundle?) {
    super.onCreate(state)

    this.setContentView(R.layout.main_host)

    if (state == null) {
      val fragment = FileChooserFragment()
      this.supportFragmentManager.beginTransaction()
        .replace(R.id.mainFragmentHost, fragment, "MAIN")
        .commit()
    }
  }
}
