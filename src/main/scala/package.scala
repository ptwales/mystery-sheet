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

  /** Indexes are integers. */
  type Index = Int

  /** Column headers are strings. */
  type Field = String

  /** A Header is an indexed sequence of fields. */
  type Header = IndexedSeq[Field]

  type Record = Map[Field, Cell]
}
