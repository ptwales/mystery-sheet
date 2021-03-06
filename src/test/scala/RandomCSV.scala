package com.ptwales.sheets

import java.nio.file.{Path, Files, Paths}
import java.nio.charset.Charset

import scala.collection.JavaConverters._
import scala.util.Random

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RandomCSV extends FunSuite {

  val maxColCount     = 5
  val maxRowCount     = 5
  val maxCellSize     = 5

  val charsetsToTest = Seq("UTF-8").map(Charset.forName(_))

  val tests = for { 
    cs <- charsetsToTest.toStream
    row <- (1 to maxRowCount)
    col <- (1 to maxColCount)
  } yield CSVTestCase(cs, row, col, maxCellSize)
    
  tests foreach { testCase =>

    val charset = testCase.charset

    test("Raw Test: " + testCase.name) {
      val sheet = CSVSheet.fromText(
        testCase.text,
        testCase.colSep,
        testCase.quote)
      testCase.check(sheet)
    }

    test("File Test: " + testCase.name) {
      val safeName = testCase.name.replace("/", "SLASH")
      val dest = Paths.get(safeName + ".csv")
      try {
        val written = Files.write(dest, testCase.lines.asJava, charset)
        val sheet = CSVSheet.fromSource(
          io.Source.fromFile(written.toFile),
          testCase.colSep,
          testCase.quote)
        testCase.check(sheet)
        Files.delete(dest)
      } finally {
      }
    }
  }
}

/** Contains test conditons for a random CSV test.
  * 
  * This should be 1 case class and 4-5 objects...
  */
case class CSVTestCase(
  charset: Charset, 
  colCount: Int,
  rowCount: Int, 
  colSep: Char,
  quote: Char,
  maxCellSize: Int)
  extends FunSuite { //probably a better class to extend


  /** Formatted string showing test conditions */
  lazy val name: String = {
    s"charset:${charset.displayName}_" +
    s"rows:${rowCount}_cols:${colCount}_" +
    s"c:${colSep}_q:${quote}"
  }

  /***************\
   Generating Data
  \***************/

  lazy val data = Vector.fill(rowCount, colCount)(
    generateRandomCell(0.25)
  )

  /** Generates random text without column separator or quote characters. */
  lazy val textGen: TextGenerator = {
    val tg = TextGenerator(charset)
    tg.forbidden += colSep
    tg.forbidden += quote
    tg
  }

  /** Generates a random cell value which might be quoted. */
  private def generateRandomCell(qProb: Double): String = {
    val s = { //prevent blank lines....
      if (colCount > 1) textGen.randomString(maxCellSize)
      else textGen.randomString(maxCellSize - 1) + 1
    }
    if (math.random < qProb) quote + s + quote
    else s
  }

  /*************\
   Printing Data
  \*************/

  /** Lines of the random data */
  lazy val lines: Seq[String] = {
    data.map(_.mkString(colSep.toString))
  }

  /** The random data as a string */
  lazy val text: String = {
    lines.mkString(System.lineSeparator)
  }

  /***************\
   Validating data
  \***************/

  def check(sheet: Table): Unit = {

    assert(sheet.rows.size == data.size)

    for (r <- (0 until data.size)) {

      val drow = data(r)
      val srow = sheet.rowAt(r)

      assert(srow.size == drow.size, s"Row $r of data=$drow")

      for (c <- (0 until srow.size)) {
        assert(srow(c) === stripQuote(drow(c)))
      }
    }
  }

  private def stripQuote(cell: Cell): Cell = {
    cell.dropWhile(_ == quote).takeWhile(_ != quote)
  }
}

/** Companion object for [[CSVTestCase]].
  */
object CSVTestCase {

  /** Generate a random [[CSVTestCase]] with size limitations. */
  def apply(charset: Charset, colCount: Int, rowCount: Int, maxCell: Int): CSVTestCase = {
    val tg = TextGenerator(charset)
    val colSep = tg.randomChar
    tg.forbidden += colSep
    val quote = tg.randomChar
    new CSVTestCase(charset, colCount, rowCount, colSep, quote, maxCell)
  }
}
