package com.ptwales.sheets

import java.nio.file.{Paths, Path, Files}

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class FileTestCSV extends FunSuite {

  val folder = Paths.get(getClass.getResource("/csv-quotes/").toURI)
  val files = Files.list(folder).iterator.asScala

  files foreach { file =>
    
  }
}
