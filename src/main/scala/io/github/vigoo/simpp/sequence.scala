package io.github.vigoo.simpp

sealed trait SeqElemBase[I]

case class SeqElem[I](elem: I) extends SeqElemBase[I]

case class SeqSeparator[I](separator: String) extends SeqElemBase[I]
