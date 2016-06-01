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

  val testsPerCharset = 1
  val maxColCount = 256
  val maxRowCount = 256
  val maxCellSize = 256
  val quoteProbability = 0.25
  val charsetsToTest = Charset.availableCharsets.asScala.values.filter(_.canEncode)

  for (charset <- charsetsToTest) {
    charsetBattery(charset, 1)
  }

  private def charsetBattery(charset: Charset, times: Int): Unit = {

    val chars = allCharsInCharset(charset)

    for (i <- (1 to testsPerCharset)) {
      testCharset(charset, chars)
    }
  }

  private def testCharset(charset: Charset, chars: Set[Char]): Unit = {

      val name = charset.displayName
      println(s"Testing Charset $name")
      var availableChars = chars

      val col = randomChar(availableChars)
      availableChars -= col

      val row = randomChar(availableChars)
      availableChars -= row

      val quote = '"' // randomDelim(availableChars)
      availableChars -= quote

      val data = randomData(availableChars.toVector, quote)
      val text = data.map(_.mkString(col.toString))

      test(s"Test $name: with c=`$col`, r=`$row`, q=`$quote`") {
        val dest = Paths.get(name)
        try {
          val written = Files.write(dest, text.asJava, charset)
          //val sheet: DataSheet = CSVSheet(written, col, row)
        } finally {
          Files.delete(dest)
        }
      }
  }
  
  private def allCharsInCharset(charset: Charset): Set[Char] = {
    val encoder = charset.newEncoder
    var result = Set[Char]()
    for (c <- (Char.MinValue to Char.MaxValue) if encoder.canEncode(c)) {
      result += c
    }
    result
  }

  private def randomChar(chars: Set[Char]): Char = {
    randomChar(chars.toVector)
  }

  private def randomChar(chars: CharList): Char = {
    val n = Random.nextInt(chars.size)
    chars(n)
  }

  private def randomData(chars: CharList, quote: Char): Data = {

    val rows = Random.nextInt(maxRowCount)
    val cols = Random.nextInt(maxColCount)
    val charSeq = chars.toVector

    def randomCell(quoteProb: Double): String = {
      val cellSize = Random.nextInt(maxCellSize)
      val cell = Seq.fill(cellSize)(randomChar(chars)).mkString
      if (math.random < quoteProb) quote + cell + quote
      else cell
    }

    Vector.fill(rows, cols)(randomCell(quoteProbability))
  }
}

