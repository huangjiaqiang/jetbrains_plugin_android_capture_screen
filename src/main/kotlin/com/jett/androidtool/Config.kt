package com.jett.androidtool

import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.absolutePathString

object Config {


    private val configFile by lazy {
        File(System.getProperty("user.home"), "jetbrains_plugin_android_util.properties").apply {
            if (!this.exists()){
                createNewFile()
            }
        }
    }

    fun loadProperties(): Properties {
        val properties = Properties()

        FileInputStream(configFile).use { fileInputStream ->
            properties.load(fileInputStream)
        }
        return properties
    }

    fun <T> put(key:String, value: T){
        loadProperties().put(key, value)
    }

    fun <T>  get(key:String): T?{
        return loadProperties().get(key) as? T
    }

}