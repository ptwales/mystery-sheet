package com.ptwales.sheets

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.nio.file.{Paths, Path, Files}

@RunWith(classOf[JUnitRunner])
class TestCSVTable extends FunSuite {

  test("Manually pass csv string with defaults") {
    val csv = "1,2,3\na,b,c"
    val sheet = CSVSheet.fromText(csv)
    assert(sheet.rows.size == 2)
    assert(sheet(0).size == 3)
  }

  test("Manually pass csv string with custom col seps") {
    val csv = "1+2+3\na+b+c"
    val sheet = CSVSheet.fromText(csv, colSep='+')
    assert(sheet.rows.size == 2)
    assert(sheet(0).size == 3)
  }

  test("Manually pass csv string with leading zeroes") {
    val csv = "01,02,03\na,b,c"
    val sheet = CSVSheet.fromText(csv)
    assert(sheet(0)(0) == Some(1))
    assert(sheet(0)(1) == Some(2))
  }

  test("Manually pass csv with quotes that protect leading zeroes") {
    val csv = "U01U,U02U,U\"03U\na,b,c"
    val sheet = CSVSheet.fromText(csv, quote='U')
    assert(sheet(0)(0) == Some("01"))
    assert(sheet(0)(1) == Some("02"))
    assert(sheet(0)(3) == Some("\"03"))
  }

  test("Manually pass csv with quotes that contain a col sep") {
    val csv = "`hello, world`\nhello"
    val sheet = CSVSheet.fromText(csv, quote='`')
    assert(sheet(0)(0) == Some("hello, world"))
  }


  test("Can use \" as column sep") {
    val csv = "1\"2\"3\na\"b\"c"
    val sheet = CSVSheet.fromText(csv, colSep='"', quote='`')
  }

/* PROBLEM FOUND: 
 *  CSVFormat.withRecordSeparator only works for printing not parsing.
 *  No custom rowSeparators allowed any more. All row separators MUST be
 *  newlines characters. Perhaps I can work around without using CSVFormat.
 *
 * See:
 *  https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html#withRecordSeparator(java.lang.String)
 *
 *  test("Manually pass csv string with custom row seps") {
 *    val csv = "1,2,3+,a,b,c"
 *    val sheet = CSVSheet.fromText(csv, rowSep="+")
 *    assert(sheet.rows.size == 2)
 *    assert(sheet.rows(0).size == 3)
 *  }
 */
}
