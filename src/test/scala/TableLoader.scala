package com.ptwales.sheets

import java.nio.file.{Paths, Path, Files}
import java.net.URL
import scala.collection.JavaConverters._

/** Simple abstraction to loading prewritten tables for testing.
  * 
  * Mostly just reducing duplicate code.
  */
object TableLoader {

  /** Loads a [[Table]] from resources */
  def loadTable(fileName: String): Table = {
    val path =  "/" + fileName
    val url = getClass.getResource(path)
    if (url != null) Table(url)
    else throw new IllegalArgumentException(s"Cannot find file $path")
  }

  /** Returns a [[Stream]] of (file name, [[Table]]) for every file in the
    * given folder.
    */
  def loadTables(folderName: String): Stream[(String, Table)] = {
    val url = getClass.getResource("/" + folderName)
    val path = Paths.get(url.toURI)
    val paths = Files.walk(path).iterator.asScala
    val files = paths.filter(Files.isRegularFile(_))
    files.map({
        f => (f.getFileName.toString, Table(f))
      }).toStream
  }
}

class TableLoader(folder: String) {

  /** Loads a [[Table]] from resources */
  def loadTable(fileName: String): Table = {
    val url = getClass.getResource("/" + fileName)
    Table(url)
  }

  /** Returns a [[Stream]] of (file name, [[Table]]) for every file in the
    * given folder.
    */
  def loadTables(folderName: String): Stream[(String, Table)] = {
    val url = getClass.getResource("/" + folderName)
    val path = Paths.get(url.toURI)
    val paths = Files.walk(path).iterator.asScala
    val files = paths.filter(Files.isRegularFile(_))
    files.map({
        f => (f.getFileName.toString, Table(f))
      }).toStream
  }

}
