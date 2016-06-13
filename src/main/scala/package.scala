package com.ptwales

package object sheets {

  /** Representation of a cell or element.
    * All are strings.
    */
  type Cell = String

  /** Row is usually a Vector of [[Cell]]s */
  type Row = IndexedSeq[Cell]

  /** Column is identical to a [[Row]], the difference is purely contextual. */
  type Column = Row

  /** Table is an indexed sequence of Rows. */
  type Table = IndexedSeq[Row]
}
