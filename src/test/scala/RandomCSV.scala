package com.ptwales.sheets

import java.nio.file.{Path, Files, Paths}
import java.nio.charset.Charset

import scala.collection.JavaConverters._
import scala.util.Random

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RandomCSVGenerator extends FunSuite {

  type Data = Vector[Vector[String]]
  type CharList = IndexedSeq[Char]

  val testsPerCharset = 15
  val maxColCount = 4
  val maxRowCount = 4
  val maxCellSize = 4

  val charsetsToTest = Seq("UTF-8").map(Charset.forName(_))

  for (charset <- charsetsToTest; i <- (1 to testsPerCharset)) {

    val tg = TextGenerator(charset)
    val name = tg.charset.displayName
    tg.forbidden += '"'

    val row = tg.randomChar
    tg.forbidden += row

    val col = tg.randomChar
    tg.forbidden += col

    val rowCount = randomSize(maxRowCount)
    val colCount = randomSize(maxColCount)

    val data = Vector.fill(rowCount, colCount)(tg.randomString(maxCellSize))
      

    val rows = data.map(_.mkString(col.toString))
    val text = rows.mkString(row.toString)
    val lines = text.split(System.lineSeparator).toVector

    test(s"charset=$name rows=$rowCount cols=$colCount c=`$col' r=`$row'") {
      val dest = Paths.get(name + ".csv")
      try {
        val written = Files.write(dest, lines.asJava, tg.charset)
        val src = io.Source.fromURL(written.toUri.toURL)
        val sheet: DataSheet = CSVSheet.fromSource(src, col, row)
        compareSheetToData(sheet, data)
      } finally {
        Files.delete(dest)
      }
    }
  }

  def randomSize(max: Int): Int = Random.nextInt(max - 1) + 1

  def compareSheetToData(sheet: DataSheet, data: Data): Unit = {

    assert(sheet.rows.size == data.size, s"First row of data=${data.head}")

    for (r <- (0 until data.size)) {

      val drow = data(r)
      val srow = sheet.rowAt(r)

      assert(srow.size == drow.size, s"Row $r of data=$drow")

      for (c <- (0 until srow.size)) {
        checkCell(srow(c), drow(c))
      }
    }
  }

  def checkCell(c: Option[Any], d: String): Unit = {
    c match {
      case Some(x) => checkValue(x.toString, d)
      case None => assertEmpty(d)
    }
  }

  def assertEmpty(s: String): Unit = {
    checkValue("", s)
  }

  def checkValue(c: String, s: String): Unit = {
    assert(c == s)
  }
}

class TextGenerator(cs: Charset) {

  val charset: Charset = cs
  var forbidden: Set[Char] = Set[Char]()

  val allChars: String = {
    val encoder = charset.newEncoder
    val charRange = (Char.MinValue.toChar to Char.MaxValue.toChar).mkString
    val encodeable: String = charRange.filter(encoder.canEncode(_))
    val noControl = encodeable.replaceAll("[\\p{C}]", "")
    val noExtension = noControl.replaceAll("[^\\p{InBasicLatin}]", "")
    noExtension
  }

  def randomChar(): Char = {
    var result: Char = '.'
    do {
      result = allChars(Random.nextInt(allChars.size))
    } while (forbidden(result))
    result
  }

  def randomString(maxSize: Int): String = {
    val size = Random.nextInt(maxSize)
    Seq.fill(size)(randomChar()).mkString
  }

}

object TextGenerator {
  def apply(cs: Charset): TextGenerator = new TextGenerator(cs)
}
