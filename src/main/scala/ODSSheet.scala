package com.ptwales.sheets

import org.odftoolkit.simple.{SpreadsheetDocument => ODSdoc}
import org.odftoolkit.simple.table.{Table => OOSheet}
import org.odftoolkit.simple.table.{Row   => OORow}
import org.odftoolkit.simple.table.{Cell  => OOCell}

import scala.collection.JavaConverters._
import java.io.InputStream

private class ODSSheet(sheet: OOSheet) extends DataSheet {

  val rows: Table = {
    val rowIter = sheet.getRowIterator.asScala
    rowIter.map(readRow(_)).toVector
  }

  private def readRow(row: OORow): Row = {
    val indexes = (0 until row.getCellCount)
    indexes.map({ 
        (r: Int) => valueOfCell(row.getCellByIndex(r))
      }).toVector
  }

  private def valueOfCell(cell: OOCell): Cell = {
    // TODO handle different types
    cell.getStringValue
  }
}

object ODSSheet {

  def fromInput(tab: Int)(istream: InputStream): DataSheet = {
    val doc = ODSdoc.loadDocument(istream)
    val sheet = doc.getSheetByIndex(tab)
    new ODSSheet(sheet)
  }
}
