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
  val maxColCount = 2
  val maxRowCount = 2
  val maxCellSize = 2
  val quoteProb = 0.25

  val charsetsToTest = Seq("UTF-8").map(Charset.forName(_))

  for (charset <- charsetsToTest; i <- (1 to testsPerCharset)) {

    val tg = TextGenerator.withOmissions(charset)
    val name = tg.charset.displayName

    val quote = '"' 

    val row = tg.randomChar
    tg.forbidden += row

    val col = tg.randomChar
    tg.forbidden += col

    val rowCount = Random.nextInt(maxRowCount)
    val colCount = Random.nextInt(maxColCount)

    val data = Vector.fill(rowCount, colCount)(
      tg.randomString(maxCellSize, quote, quoteProb))

    val rows = data.map(_.mkString(col.toString))
    val text = rows.mkString(row.toString)
    val lines = text.split(System.lineSeparator).toVector

    test(s"Test $name: with c=`$col`, r=`$row`, q=`$quote`") {
      val dest = Paths.get(name + ".csv")
      try {
        val written = Files.write(dest, lines.asJava, tg.charset)
        val sheet: DataSheet = CSVSheet(written, col, row)
        compareSheetToData(sheet, data)
      } finally {
        Files.delete(dest)
      }
    }
  }

  private def compareSheetToData(sheet: DataSheet, data: Data): Unit = {
    assert(sheet.rows.size == data.size)
    for (r <- (0 until data.size)) {

      val drow = data(r)
      val srow = sheet.rowAt(r)
      assert(srow.size == drow.size)

      for (c <- (0 until srow.size)) {
        srow(c) match {
          case Some(x) => assert(x == drow(c))
          case None => assert(drow(c) == "")
        }
      }
    }
  }
}

class TextGenerator(cs: Charset) {

  val charset: Charset = cs
  var forbidden: Set[Char] = Set[Char]()

  val allChars: IndexedSeq[Char] = {
    val encoder = charset.newEncoder
    val charRange = (Char.MinValue to Char.MaxValue)
    charRange.filter(encoder.canEncode(_))
  }

  def randomChar(): Char = {
    var result: Char = '.'
    do {
      result = allChars(Random.nextInt(allChars.size))
    } while (forbidden(result))
    result
  }

  def randomString(maxSize: Int, q: Char, qp: Double): String = {
    val size = Random.nextInt(maxSize)
    val s = Seq.fill(size)(randomChar()).mkString
    if (math.random < qp) q + s + q
    else s
  }

}

object TextGenerator {

  def apply(cs: Charset): TextGenerator = new TextGenerator(cs)

  def withOmissions(cs: Charset, om: Iterable[Char]): TextGenerator = {
    val result = new TextGenerator(cs)
    result.forbidden ++= om
    result
  }

  def withOmissions(cs: Charset, om: Char*): TextGenerator = {
    withOmissions(cs, om)
  }
}
