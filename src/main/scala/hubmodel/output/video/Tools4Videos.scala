package hubmodel.output.video

import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D}

import breeze.numerics.floor
import hubmodel.output.border
import hubmodel.{NewBetterPosition2D, NewTime}

/**
  * Collection of various tools for formatting data, drawing elements and mapping coordinates.
  */
trait Tools4Videos {



  /** Combines a List of (Time, Position) tuples into a List of (Time, List[Position]) tuples.
    * This makes drawing process easier.
    *
    * @param hist original data formatted as List[(Time, Position)]
    * @return reformating of data as List[(Time, List[Position])]
    */
  def mergeListsByTime(hist: List[(NewTime, NewBetterPosition2D)]): List[(NewTime, List[NewBetterPosition2D])] = {
    hist.groupBy(_._1).map { case (k, v) => (k, v.map(_._2)) }.toList
  }

  /** Template function for creating dots representing pedestrians.
    * The first group of parameters are the sizes in pixels and meters for the mapping functions.
    * The second group of parameters is only composed of one parameters, and is simply the position
    * at which to draw the dot.
    *
    * @param hMeters height in meters of the image
    * @param hPixels height in pixels of the image
    * @param wMeters width in meters of the image
    * @param wPixels width in pixels of the image
    * @param pos     position to draw the dot
    * @return an ellipse2D object to draw using the fill method from[[Graphics2D]]
    */
  def createDot(mappingFunctions: (Double => Int, Double => Int), size: Double)(pos: NewBetterPosition2D): Ellipse2D = {
    new Ellipse2D.Double(mappingFunctions._1(pos.X), mappingFunctions._2(pos.Y), size, size)
  }

  /** Mapping function for horizontal (width) coordinates
    *
    * @param trueSize height in meters of the image
    * @param pixelSize height in pixels of the image
    * @param coord    point to map
    * @return the position in pixels of the original coordinate
    */
  def  mapCoordLinear(trueSize: Double, pixelSize: Int)(coord: Double): Int = floor(coord / trueSize * pixelSize).toInt



  def createWhiteBackground(bkgdImageSizeMeters: (Double, Double)): BufferedImage = {

    val initialWidth: Int = bkgdImageSizeMeters._1.ceil.toInt * 20
    val initialHeight: Int = bkgdImageSizeMeters._2.ceil.toInt * 20

    // rounds the canvas width to an even number
    val canvasWidth: Int = if (initialWidth % 2 == 0) initialWidth else initialWidth + 1
    val canvasHeight: Int = if (initialHeight % 2 == 0) initialHeight else initialHeight + 1
    val canv: BufferedImage = new BufferedImage(border*2 + canvasWidth, border*2 + canvasHeight, BufferedImage.TYPE_4BYTE_ABGR)
    val gcanv: Graphics2D = canv.createGraphics()
    gcanv.setColor(Color.WHITE)
    gcanv.fillRect(0, 0, border*2 + canvasWidth, border*2 + canvasHeight)
    canv
  }

}