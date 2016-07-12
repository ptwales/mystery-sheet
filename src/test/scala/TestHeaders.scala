package com.ptwales.sheets

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestHeaderAPI extends FunSuite {

  val sheet = TableLoader.loadTable("headers/header-table-lf.csv")
  val header = Seq("int", "char")
  val table = HeadedSheet(sheet, header)

  test("First row is not the header") {
    assert(table.rowAt(0) != header)
  }

  test("Get correct indexes of fields") {
    for ((f, i) <- header.zipWithIndex) {
      assert(table.indexOf(f) == i)
    }
  }

  test("Get cols by name.") {
    val ints = table.colAt("int")
    assert(ints == Seq(1, 2, 3, 4, 5).map(_.toString))
    val chars = table.colAt("char")
    assert(chars.mkString == "abcde")
  }

  test("Can get records") {
    val rec = table.recordAt(0)
    assert(rec("int")  == "1")
    assert(rec("char") == "a")
    assert(rec.keySet == header.toSet)
  }
}

