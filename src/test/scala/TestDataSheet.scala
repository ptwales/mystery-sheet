package com.ptwales.sheets

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.nio.file.{Paths, Path, Files}
import java.net.URL
import scala.collection.JavaConverters._

object TableLoader {

  def loadTable(fileName: String): DataSheet = {
    val url = getClass.getResource("/" + fileName)
    DataSheet(url)
  }

  def loadTables(folderName: String): Stream[(String, DataSheet)] = {
    val url = getClass.getResource("/" + folderName)
    val path = Paths.get(url.toURI)
    val paths = Files.walk(path).iterator.asScala
    val files = paths.filter(Files.isRegularFile(_))
    files.map({
        f => (f.getFileName.toString, DataSheet(f))
      }).toStream
  }
}


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
      val table = TableLoader.loadTable(file)
      assert(table.rows.size == 5)
    }
  }

}

