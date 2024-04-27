package com.jett.androidtool

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.*


internal class CaptureScreenAction : AnAction() {
    //    private val imageLabel = ResizeImageLabel()
    private val imageLabel = ResizeImageLabel()
    private val captureButton = JButton("Capture Screen")



    override fun actionPerformed(e: AnActionEvent) {

        val frame = JFrame().apply {
            isResizable = true
            defaultCloseOperation = JFrame.HIDE_ON_CLOSE
            setSize(500, 800)
            preferredSize = Dimension(500, 800)
            minimumSize = Dimension(300, 500)

            // Set layout to BorderLayout
            layout = BorderLayout()

            add(imageLabel, BorderLayout.CENTER)

            add(JPanel().apply {
                add(captureButton)
            }, BorderLayout.SOUTH)

            isVisible = true
        }

        imageLabel.onCaptureSelect = {it, error->

            it?.let {
                val path: String = Paths.get(e.project?.basePath!!).toAbsolutePath().toString()
                val name = JOptionPane.showInputDialog(null, "请输入文件名")
                if (name == null || name.isEmpty()){
                    return@let
                }

                val outputfile: File = File("$path/images/$name.png")
                val mkdirs: Boolean = outputfile.getParentFile().mkdirs()
                // 创建一个弹窗

                ImageIO.write(it, "png", outputfile)
                writeContentToEditDocument(e, outputfile.absolutePath)
            }
            error?.let {
                Messages.showErrorDialog("Fail to Capture Screen.:${it.message}", "Error")
            }
        }
        captureButton.addActionListener { event: ActionEvent? ->
            captureButton.text = "Loading..."
            captureButton.isEnabled = false
            GlobalScope.launch() {
                val imageIcon = kotlin.runCatching {
                    val capturedImage = captureScreenWithAdb()
                    ImageIcon(capturedImage)
                }.onFailure {
                    it.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Messages.showErrorDialog("Fail to Capture Screen.:${it.message}", "Error")
                    }
                }.getOrNull()
                withContext(Dispatchers.Default) {
                    imageLabel.originalImage = imageIcon?.image
                    captureButton.text = "Capture Screen"
                    captureButton.isEnabled = true
                    imageLabel.repaint()
                    frame.pack()
                }
            }
        }
    }


    private fun writeContentToEditDocument(e: AnActionEvent, content:String) {

         val insertContent = content.replace("\\", "\\\\")
        // 获取当前的项目和编辑器对象
        val project: Project = e.getProject()!!
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        if (editor == null) return

        val primaryCaret: Caret = editor.getCaretModel().getPrimaryCaret() // 获取光标信息
        val document: Document = editor.getDocument()
        // 运行一个写操作，因为插入文本到文档需要在写模式下完成
        WriteCommandAction.runWriteCommandAction(
            project
        ) { document.insertString(primaryCaret.getOffset(), insertContent) }

        primaryCaret.moveToOffset(primaryCaret.getOffset() + insertContent.length) // 移动光标到新内容后面
    }

    @Throws(Exception::class)
    private fun captureScreenWithAdb(): BufferedImage {
        /* Call 'adb' command and capture the screen image of your Android device here */
        val rt = Runtime.getRuntime()
        val commands = arrayOf("adb", "exec-out", "screencap", "-p")
        val proc = rt.exec(commands)
        return ImageIO.read(proc.inputStream)
    }
}