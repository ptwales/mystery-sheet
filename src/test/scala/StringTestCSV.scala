package com.ptwales.sheets

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StringTestCSV extends FunSuite {

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
    assert(sheet(0)(0) == "01")
    assert(sheet(0)(1) == "02")
  }

  test("Manually pass csv with quotes to protect leading zeroes") {
    val csv = "U01U,U02U,U\"03U\na,b,c"
    val sheet = CSVSheet.fromText(csv, quote='U')
    assert(sheet(0)(0) == "01")
    assert(sheet(0)(1) == "02")
    assert(sheet(0)(2) == "\"03")
  }

  test("Manually pass csv with quotes that contain a col sep") {
    val csv = "`hello, world`\nhello"
    val sheet = CSVSheet.fromText(csv, quote='`')
    assert(sheet(0)(0) == "hello, world")
  }

  test("Can use \" as column sep") {
    val csv = "1\"2\"3\na\"b\"c"
    val sheet = CSVSheet.fromText(csv, colSep='"', quote='`')
    assert(sheet(0)(1) == "2")
    assert(sheet(1)(2) == "c")
  }

  test("Single column") {
    val csv = "1\n2\n3"
    val sheet = CSVSheet.fromText(csv, colSep='M')
    assert(sheet.colAt(0).mkString("\n") == csv)
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
