package com.ptwales.sheets

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestDataSheet extends FunSuite {

  val chars = "abcdefghijklmnopqrstuvwxyz".toList
  val tables = TableLoader.loadTables("data-table")

  for ((file, table) <- tables) {
    test(s"Can get values of $file") {
      var r = 0
      table.rows foreach {
        (row) => {
          assert(row(0).toDouble.toInt == r + 1)
          assert(chars(r).toString     == row(1))
          r += 1
        }
      }
    }
  }
}

@RunWith(classOf[JUnitRunner])
class TestTrailing extends FunSuite {

  val tables = TableLoader.loadTables("trailing")
  for ((file, table) <- tables) {
    test(s"trailing rows are trimmed from: $file") {
      assert(table.rows.size == 5)
    }
  }

}

