package com.ptwales.sheets

import org.odftoolkit.simple.{SpreadsheetDocument => ODSdoc}
import org.odftoolkit.simple.table.{Table => OOSheet}
import org.odftoolkit.simple.table.{Row   => OORow}
import org.odftoolkit.simple.table.{Cell  => OOCell}

import scala.collection.JavaConverters._
import java.io.InputStream

private class ODSSheet(sheet: OOSheet) extends Table {

  val rows: IndexedSeq[Row] = {
    val rs = sheet.getRowIterator.asScala
    val rvs = rs.map(readRow(_))
    rvs.takeWhile(_.length > 0).toVector
  }

  private def readRow(row: OORow): Row = {
    val indexes = (sheet.getColumnCount - 1 to 0 by -1)
    val cellVals = indexes.map(readCell(row))
    val usedCells = cellVals.dropWhile(_.trim == "")
    usedCells.reverse.toVector
  }

  private def readCell(row: OORow)(index: Int): Cell = {
    val cell: OOCell = row.getCellByIndex(index)
    valueOfCell(cell)
  }

  private def valueOfCell(cell: OOCell): Cell = {
    // TODO handle different types
    cell.getStringValue
  }
}

object ODSSheet {

  def fromInput(tab: Int)(istream: InputStream): Table = {
    val doc = ODSdoc.loadDocument(istream)
    val sheet = doc.getSheetByIndex(tab)
    new ODSSheet(sheet)
  }
}
