package com.gabeos.wafflemusic

import java.awt.{GridLayout, BorderLayout}
import java.awt.event.{WindowEvent, WindowAdapter}
import javax.swing.{JPanel, JApplet, JFrame}
import com.gabeos.wafflemusic.camera.{ImageInfo, DepthImageInfo}
import com.gabeos.wafflemusic.processing.BasicProcessor
import com.gabeos.wafflemusic.ui.{DataImage, MonitorImage}
import com.typesafe.scalalogging.LazyLogging
import org.opencv.core.Core
import org.opencv.highgui.VideoCapture
import org.opencv.videoio.Videoio._
import rx._
import rx.ops._
import de.sciss.synth._
import Cfg._
import com.gabeos.wafflemusic.enrichment._

/**
 * Created by Gabriel Schubiner on 7/29/2014.
 */

object WaffleMusic extends App with LazyLogging {
  // Boot server
//  val serverCxn = Server.boot
//  serverCxn.addListener({ case ServerConnection.Aborted =>
//    Server.default.quit()
//  })

  def exit(status: Int, msg: String) = {
//    serverCxn.abort()
    logger.info(msg)
    System.exit(status)
  }

  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
  val capture = new VideoCapture(CAP_INTELPERC)
  if (!capture.isOpened()) exit(0,"Capture couldn't open camera")

  if (!capture.set(CAP_INTELPERC_DEPTH_GENERATOR | CAP_PROP_INTELPERC_PROFILE_IDX, 0.0)) exit(0, "Capture couldn't set up depth stream")
  if (!capture.set(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_INTELPERC_PROFILE_IDX, 0.0)) exit(0, "Capture couldn't set up image stream")

  implicit val depthInfo = DepthImageInfo(capture)
  implicit val imgInfo = ImageInfo(capture)

  // init reactive vars
  val imageData: Var[ImageData] = Var(ImageData())

  // init monitor
  val frame: JFrame = new JFrame("WaffleMusic!!")
  val monitor = new JPanel()
  monitor.setLayout(new GridLayout(1,3))
  val df = new MonitorImage(imageData)
  monitor.add(df)
  frame.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) {
      exit(0,"Window Closed")
    }
  })
  frame.getContentPane.add(monitor)
  frame.setSize(imgInfo.width,imgInfo.height)
  frame.setVisible(true)
  logger.info(s"JFrame Initialized")

  if (capture.grab()) {
    // Fill all data Mats
    capture.retrieve(imageData().depth, CAP_INTELPERC_DEPTH_MAP)
    capture.retrieve(imageData().image, CAP_INTELPERC_IMAGE)
    capture.retrieve(imageData().uvMap, CAP_INTELPERC_UVDEPTH_MAP)
    imageData().depth.depth2ImageCoord(imageData().uvMap, imageData().image.size(), imageData().imgDepth)
  }

  val proc = new BasicProcessor(imageData)
  val df1 = new DataImage(imgInfo.width,imgInfo.height,proc.cannyImg)
  val df2 = new DataImage(depthInfo.width,depthInfo.height,proc.normalizedDepth)
  monitor.add(df1)
  monitor.add(df2)
  monitor.setSize(imgInfo.width*3,imgInfo.height)
  frame.setSize(imgInfo.width*3,imgInfo.height)
  // Instantiate reactively activated sound synth
//  val synth = new BasicSoundKit(image, depthMap, validDepths, validRaster, samplesTaken) with BasicProcessor

  // main loop
  while (true) {
    if (capture.grab()) {
      // Fill all data Mats
      capture.retrieve(imageData().depth,CAP_INTELPERC_DEPTH_MAP)
      capture.retrieve(imageData().image,CAP_INTELPERC_IMAGE)
      capture.retrieve(imageData().uvMap,CAP_INTELPERC_UVDEPTH_MAP)
      imageData().depth.depth2ImageCoord(imageData().uvMap,imageData().image.size(), imageData().imgDepth)

      // increment sample count
      imageData().increment()

      // propagate data
      imageData.propagate()
//      logger.debug(s"DepthMat (type, depth, channels, size(w,h), total): ${rawDepthMap.`type`()}, ${rawDepthMap.depth()}, ${rawDepthMap.channels()}, (${rawDepthMap.size().width},${rawDepthMap.size().height}), ${rawDepthMap.total()}")
//      logger.debug(s"ImageMat (type, depth, channels, size(w,h), total): ${rawDepthMap.`type`()}, ${image.depth()}, ${image.channels()}, (${image.size().width},${image.size().height}), ${image.total()}")
//      logger.debug(s"uvMat    (type, depth, channels, size(w,h), total): ${rawDepthMap.`type`()}, ${uvMap.depth()}, ${uvMap.channels()}, (${uvMap.size().width},${uvMap.size().height}), ${uvMap.total()}")
    }
  }
}

