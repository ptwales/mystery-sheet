package com.ptwales.sheets

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.nio.file.{Paths, Path, Files}

object TableLoader {
  def apply(fileName: String): DataSheet = {
    val url = getClass.getResource("/" + fileName)
    DataSheet(url)
  }
}

@RunWith(classOf[JUnitRunner])
class TestCSVTable extends FunSuite {

  test("Manually pass csv string with defaults") {
    val csv = "1,2,3\n,a,b,c"
    val sheet = CSVSheet.fromText(csv)
    assert(sheet.rows.size == 2)
    assert(sheet.rows(0).size == 3)
  }

  test("Manually pass csv string with custom col seps") {
    val csv = "1+2+3\n+a+b+c"
    val sheet = CSVSheet.fromText(csv, '+')
    assert(sheet.rows.size == 2)
    assert(sheet.rows(0).size == 3)
  }

}

@RunWith(classOf[JUnitRunner])
class TestDataSheet extends FunSuite {

  val files = Seq(".xls", ".xlsx", "-lf.csv", "-crlf.csv", "-eol.csv") map { 
    "data-table" + _
  }

  val chars = "abcdefghijklmnopqrstuvwxyz".toList
  for (file <- files) {

    test(s"Can get values of $file") {

      val table = TableLoader(file)
      var r = 0
      table.rows foreach {
        (row) => {
          assert(row(0).get === (r + 1))
          assert(row(1).get === chars(r).toString)
          r += 1
        }
      }
    }
  }

  // TODO: more tests listed below
  // test("headers can be ignored")

}

@RunWith(classOf[JUnitRunner])
class TestTrailing extends FunSuite {

  val files = Seq(
    "one-trailing-nl.csv",
    "two-trailing-nl.csv",
    "one-trailing.xls",
    "two-trailing.xls",
    "one-trailing.xlsx",
    "two-trailing.xlsx"
  )

  for (file <- files) {
    test(s"trailing rows are trimmed from: $file") {
      val table = TableLoader(file)
      assert(table.rows.size == 5)
    }
  }

}

