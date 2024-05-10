package com.jett.androidtool

import com.intellij.vcs.commit.commitProperty
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

//    private val properties by lazy {
//        loadProperties()
//    }

    private val properties : Properties
        get() {
            return loadProperties()
        }

    fun loadProperties(): Properties {
        val properties = Properties()

        FileInputStream(configFile).use { fileInputStream ->
            properties.load(fileInputStream)
        }
        return properties
    }

    fun <T> put(key:String, value: T){
        properties.put(key, value)
        saveProperties()
    }

    fun saveProperties() {
        FileOutputStream(configFile).use { fileOutputStream ->
            properties.store(fileOutputStream, null)
        }
    }

    fun <T>  get(key:String): T?{
        return properties.get(key) as? T
    }

}