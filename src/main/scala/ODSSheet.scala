package com.ptwales.sheets

import org.jopendocument.dom.spreadsheet.{Table => JOSheet}
import org.jopendocument.dom.spreadsheet.{Cell => JOCell}

class ODSSheet(sheet: JOSheet[_]) extends DataSheet {

  val rows: Table = (0 until sheet.getRowCount) map { r =>
    (0 until sheet.getColumnCount) map { c =>
      valueOfCell(sheet.getCellAt(r, c))
    }
  }

  private def valueOfCell[T](cell: JOCell[_]): Cell = {
    cell.getValue.toString
  }
}
