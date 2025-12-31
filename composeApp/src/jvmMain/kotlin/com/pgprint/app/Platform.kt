package com.pgprint.app

class JVMPlatform {
    val name: String = "Java ${System.getProperty("java.version")}"
    val isDebug: Boolean = System.getProperty("debug")?.toBoolean() == true

}

fun getPlatform() = JVMPlatform()