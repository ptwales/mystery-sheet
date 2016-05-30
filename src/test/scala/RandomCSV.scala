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

  val charsetsToTest = Charset.availableCharsets.asScala
  val testsPerCharset = 5
  for ( i <- (1 to testsPerCharset)) {

    var chars: Set[Char] = ???

    val col = randomDelim(chars)
    chars -= col

    val row = randomDelim(chars)
    chars -= row

    val quote = '"' // randomDelim(chars)
    chars -= quote

    val data = randomData(chars, quote)
    val text = data.map(_.mkString(col.toString))

    for ((name, charset) <- charsetsToTest) {
      test(s"Test #$i in $name: with c=$col, r=$row, q=$quote") {
        // write text to file in charset
        val dest = Paths.get(getClass.getResource("/random-csv/" + name).toURI)
        val written = Files.write(dest, text.asJava, charset)
        // read as CSVSheet
        val sheet: DataSheet = CSVSheet(written, col, row)
        // check that data matches
        //???
        // delete file
        Files.delete(written)
      }
    }
  }

  private def randomDelim(chars: Set[Char]): Char = {
    Random.shuffle(chars.toList).head
  }

  private def randomData(chars: Set[Char], quote: Char): Data = {

    val rows = Random.nextInt(256)
    val cols = Random.nextInt(256)

    def randomCell(quoteProb: Double): String = {
      val cellSize = Random.nextInt(chars.size)
      val cell = Random.shuffle(chars.toList).take(cellSize).mkString
      if (math.random < quoteProb) quote + cell + quote
      else cell
    }

    Vector.fill(rows, cols)(randomCell(0.25))
  }
}

