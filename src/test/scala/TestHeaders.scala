package com.ptwales.sheets

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestHeaderAPI extends FunSuite {

  val sheet = TableLoader.loadTable("headers/header-table-lf.csv")
  val header = Vector("int", "char")
  val battery = new HeaderBattery(sheet, header)
  battery.check
}

class HeaderBattery(sheet: DataSheet, header: Header) extends FunSuite {

  val table = HeadedSheet(sheet, header)

  def check(): Unit = {

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
      for ((field, index) <- header.zipWithIndex) {
        assert(sheet.colAt(index) == table.colAt(field))
      }
    }

    test("Can get records") {
      for (r <- (0 until table.rows.size); c <- (0 until header.size)) {
        val rec = table.recordAt(r)
        assert(rec.keySet == header.toSet)
        assert(header.size == sheet(r).size)
        assert(rec(header(c)) == sheet(r)(c))
      }
    }
  }
}
