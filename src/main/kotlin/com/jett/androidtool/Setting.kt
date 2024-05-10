package com.jett.androidtool
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


fun showSettingWindow(){
    // 设置按钮点击时，打开新窗口
    val settingsFrame = JFrame("Settings").apply {
        setSize(300, 200) // 设置新弹窗的尺寸
        preferredSize = Dimension(300, 200)
        layout = FlowLayout() // 设置布局

        val savedPathText = Config.get<String>("adb") // 尝试加载已保存的路径
        val pathTextField = JTextField().apply {
            preferredSize = Dimension(200, 30)
            if (savedPathText?.isNotEmpty() == true) {
                text = savedPathText
            }

            // 支持文件拖拽
            dropTarget =  DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,  object:DropTargetAdapter() {


                override fun drop(dtde: DropTargetDropEvent?) {
                    try {
                        dtde?.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        val droppedFiles: List<File> = dtde?.transferable?.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                        if (droppedFiles.isNotEmpty()) {
                            // 只处理第一个文件
                            val file = droppedFiles.get(0);
                            text = file.absolutePath;
                        }
                    } catch ( ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }, true, null);
        }
        val saveButton = JButton("Save").apply {
            addActionListener {// 保存路径到本地文件
                Config.put("adb", pathTextField.text)
            }
        }
        add(JPanel().apply {
            add(pathTextField)
            add(saveButton)
        }, BorderLayout.SOUTH)

        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        setLocationRelativeTo(null)
        pack() // 根据组件调整窗口大小
        isVisible = true
    }
}

private fun saveAdbPath(filePath: String) {
    try {
        // 假设保存文件至用户目录下的settings.txt
        Files.write(Paths.get(System.getProperty("user.home"), "settings.txt"), filePath.toByteArray())
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

private fun loadSavedPath(): String {
    try {
        val path = Paths.get(System.getProperty("user.home"), "settings.txt")
        if (Files.exists(path)) {
            return Files.readString(path)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return ""
}