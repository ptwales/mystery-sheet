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
  val maxColCount = 32
  val maxRowCount = 32
  val maxCellSize = 32
  val quoteProb = 0.25

  val charsetsToTest = Seq("UTF-8"
  ).map(Charset.forName(_))

  for (charset <- charsetsToTest) {
    charsetBattery(charset, 1)
  }

  private def charsetBattery(charset: Charset, times: Int): Unit = {

    for (i <- (1 to testsPerCharset)) {
      val tg = new TextGenerator(charset)
      testCharset(tg)
    }
  }

  private def testCharset(tg: TextGenerator): Unit = {

      val name = tg.charset.displayName

      val quote = '"' 
      tg.forbiddenChars += quote

      val row = tg.randomChar
      tg.forbiddenChars += row

      val col = tg.randomChar
      tg.forbiddenChars += col

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
        } finally {
          Files.delete(dest)
        }
      }
  }
}

class TextGenerator(cs: Charset) {

  val charset: Charset = cs
  var forbiddenChars: Set[Char] = Set[Char]()

  val allChars: IndexedSeq[Char] = {
    val encoder = charset.newEncoder
    val charRange = (Char.MinValue to Char.MaxValue)
    charRange.filter(encoder.canEncode(_))
  }

  def randomChar(): Char = {
    var result: Char = '.'
    do {
      result = allChars(Random.nextInt(allChars.size))
    } while (forbiddenChars(result))
    result
  }

  def randomString(maxSize: Int, quote: Char, qProb: Double): String = {
    val cellSize = Random.nextInt(maxSize)
    val cell = Seq.fill(cellSize)(randomChar()).mkString
    if (math.random < qProb) quote + cell + quote
    else cell
  }

}

