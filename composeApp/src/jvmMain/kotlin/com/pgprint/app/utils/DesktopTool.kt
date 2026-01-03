package com.pgprint.app.utils

import java.awt.Desktop
import java.io.File
import java.net.URI


object DesktopTool {

    fun openBrowser(url: String) {
        Desktop.getDesktop().browse(URI(url))
    }

    fun openFile(file: File) {
        Desktop.getDesktop().open(file)
    }
}