package com.ptwales.sheets

import java.nio.file.{Paths, Path, Files}
import java.net.URL
import scala.collection.JavaConverters._

/** Simple abstraction to loading prewritten tables for testing.
  * 
  * Mostly just reducing duplicate code.
  */
object TableLoader {

  /** Loads a [[DataSheet]] from resources */
  def loadTable(fileName: String): DataSheet = {
    val url = getClass.getResource("/" + fileName)
    DataSheet(url)
  }

  /** Returns a [[Stream]] of (file name, [[DataSheet]]) for every file in the
    * given folder.
    */
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
