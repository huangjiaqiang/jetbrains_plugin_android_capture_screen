package com.jett.androidtool

import com.intellij.util.ui.ImageUtil
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.IOException
import javax.swing.JLabel
import kotlin.math.max
import kotlin.math.min


class ResizeImageLabel(var originalImage: Image?=null) : JLabel() {



     init{
        val mouseHandler: MouseHandler = MouseHandler()
        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)
    }

    var imageScale = 0f
    var imageRect : Rectangle = Rectangle()

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        originalImage?:return
        // Calculate the correct size of the image
        val width = this.width
        val height = this.height
        val imageWidth = originalImage!!.getWidth(this)
        val imageHeight = originalImage!!.getHeight(this)
        imageScale =
            min((width.toFloat() / imageWidth).toDouble(), (height.toFloat() / imageHeight).toDouble()).toFloat()
        val newImageWidth = (imageWidth * imageScale).toInt()
        val newImageHeight = (imageHeight * imageScale).toInt()

        // Center the image to the label
        val x = (width - newImageWidth) / 2
        val y = (height - newImageHeight) / 2

        // Draw image with the correct size
        imageRect.let {
            it.x = x
            it.y = y
            it.width = newImageWidth
            it.height = newImageHeight
        }
        g.drawImage(originalImage, x, y, newImageWidth, newImageHeight, this)


        if (selection != null) {
            val g2d = g.create() as Graphics2D
            g2d.color = Color(0, 0, 255, 128)
            g2d.fill(selection)
            g2d.color = Color.BLUE
            g2d.draw(selection)
            g2d.dispose()
        }
    }

    var onCaptureSelect: ((image:BufferedImage?, error:Throwable? )->Unit)? =null

    fun captureSelectedImage() {
        try {
            val bufferedImage = ImageUtil.createImage(originalImage!!.getWidth(this), originalImage!!.getHeight(this),
                BufferedImage.TYPE_INT_RGB)

            val g: Graphics = bufferedImage.createGraphics()
            g.drawImage(originalImage, 0, 0, null)
            g.dispose()

            val imageScale = imageRect.width.toDouble() / bufferedImage.width.toDouble()
            println("imageScale = $imageScale")
            val x = (max(selection!!.x-imageRect.x, 0))/imageScale
            val y = (max(selection!!.y-imageRect.y, 0))/imageScale
            val w = min(selection!!.width, imageRect.width) /imageScale
            val h = min(selection!!.height, imageRect.height) /imageScale
            println("x: $x, y: $y, w: $w, h: $h")

            val subImage = bufferedImage.getSubimage(x.toInt(), y.toInt(), w.toInt(), h.toInt())
            onCaptureSelect?.invoke(subImage, null)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            onCaptureSelect?.invoke(null, ioException)
        }
    }

    private var selection: Rectangle? = null
    private var anchor: Point? = null
    private inner class MouseHandler : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            anchor = e.point
            selection = Rectangle(anchor)
            repaint()
        }

        override fun mouseDragged(e: MouseEvent) {
            anchor?.let { anchor->
                selection?.setBounds(
                    Math.min(anchor.x, e.x), Math.min(anchor.y, e.y),
                    Math.abs(anchor.x - e.x), Math.abs(anchor.y - e.y)
                )
            }

            repaint()
        }

        override fun mouseReleased(e: MouseEvent) {

            selection?.let { selection ->
                if (selection.width <= 0 || selection.height <= 0) {
                    this@ResizeImageLabel.selection = null
                    repaint()
                }
                captureSelectedImage()
            }

        }
    }
}