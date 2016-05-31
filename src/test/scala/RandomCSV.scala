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

  //val charsetsToTest = Charset.availableCharsets.asScala.values filter {
  //  _.canEncode
  //}

  val charsetsToTest = Set("US-ASCII", "IBM037") map { Charset.forName(_) }
  val testsPerCharset = 10
  val maxColCount = 256
  val maxRowCount = 256
  val maxCellSize = 256
  val quoteProbability = 0.25

  for (charset <- charsetsToTest) {

    val chars = allCharsInCharset(charset)

    for (i <- (1 to testsPerCharset)) {

      val name = charset.displayName
      var availableChars = chars

      val col = randomChar(availableChars)
      availableChars -= col

      val row = randomChar(availableChars)
      availableChars -= row

      val quote = '"' // randomDelim(availableChars)
      availableChars -= quote

      val data = randomData(availableChars, quote)
      val text = data.map(_.mkString(col.toString))

      test(s"Test #$i in $name: with c=`$col`, r=`$row`, q=`$quote`") {
        val dest = Paths.get(name)
        val written = Files.write(dest, text.asJava, charset)
        //val sheet: DataSheet = CSVSheet(written, col, row)
        Files.delete(written)
      }
    }
  }

  private def allCharsInCharset(charset: Charset): Set[Char] = {
    val encoder = charset.newEncoder
    var result = Set[Char]()
    for (c <- (Char.MinValue to Char.MaxValue) if encoder.canEncode(c)) {
      result += c
    }
    return result
  }

  private def randomChar(chars: Set[Char]): Char = {
    val n = Random.nextInt(chars.size)
    chars.iterator.drop(n).next
  }

  private def randomData(chars: Set[Char], quote: Char): Data = {

    val rows = Random.nextInt(maxRowCount)
    val cols = Random.nextInt(maxColCount)

    def randomCell(quoteProb: Double): String = {
      val cellSize = Random.nextInt(maxCellSize)
      val cell = Seq.fill(cellSize)(randomChar(chars)).mkString
      if (math.random < quoteProb) quote + cell + quote
      else cell
    }

    Vector.fill(rows, cols)(randomCell(quoteProbability))
  }
}

