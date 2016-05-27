package com.ptwales

package object DataSheet {

  /** Representation of a cell or element.  None if blank. */
  type Cell = Option[Any]

  /** Row is usually a Vector of [[Cell]]s */
  type Row = IndexedSeq[Cell]

  /** Column is identical to a [[Row]], the difference is purely contextual. */
  type Column = Row

  /** Table is an indexed sequence of Rows. */
  type Table = IndexedSeq[Row]
}
