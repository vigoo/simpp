package io.github.vigoo.simpp

import cats.Foldable
import cats.data.{State, Writer}
import cats.implicits._
import org.atnos.eff.Members.&&:
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

object PrettyPrint {
  case class PrettyPrinterState(indentationSize: Int, indentation: Int, atLineStart: Boolean)

  type PrettyPrinterContext[AdditionalFx] =
    Fx.append[Fx.fx2[Writer[String, ?], State[PrettyPrinterState, ?]], AdditionalFx]

  type _state[R] = State[PrettyPrinterState, ?] |= R
  type _writer[R] = Writer[String, ?] |= R
  type _prettyPrinter[R] = _state[R] &&: _writer[R]

  def write[R: _writer](s: String): Eff[R, Unit] =
    tell[R, String](s)

  def getState[R: _state]: Eff[R, PrettyPrinterState] =
    get[R, PrettyPrinterState]

  def modifyState[R: _state](f: PrettyPrinterState => PrettyPrinterState): Eff[R, Unit] =
    modify[R, PrettyPrinterState](f)
}

trait PrettyPrint[AdditionalFx] {
  import PrettyPrint._
  type R = PrettyPrinterContext[AdditionalFx]
  type PP[A] = Eff[R, A]

  def runAdditionalFx(f: PP[Unit]): Eff[PrettyPrinterContext[NoFx], Unit]

  def changeIndentation(newValue: Int): PP[Unit] =
    PrettyPrint.modifyState[R](_.copy(indentationSize = newValue))

  def append(char: Char): PP[Unit] =
    if (char == '\n') {
      startNewLine()
    } else {
      for {
        _ <- indentIfNeeded()
        _ <- write[R](char.toString)
      } yield ()
    }

  def append(str: String): PP[Unit] = {
    val lines = Foldable[List].intercalate(
      str.lines.toList.map(line => List(line)),
      List("\n"))

    def printLines(lines: List[String]): PP[Unit] =
      lines match {
        case Nil => empty
        case "\n" :: remaining => startNewLine() >> printLines(remaining)
        case other :: remaining => indentIfNeeded() >> write[R](other) >> printLines(remaining)
      }
    printLines(lines) >> {
      if (str.endsWith("\n")) {
        startNewLine()
      } else {
        empty
      }
    }
  }

  def startNewLine(): PP[Unit] =
    for {
      _ <- write[R]("\n")
      _ <- modifyState[R](_.copy(atLineStart = true))
    } yield ()

  def indent(): PP[Unit] =
    modifyState[R](state => state.copy(indentation = state.indentation + 1))

  def unindent(): PP[Unit] = {
    // TODO: error handling
    modifyState[R](state => state.copy(indentation = state.indentation - 1))
  }

  private def indentIfNeeded(): PP[Unit] = {
    getState[R].flatMap { state =>
      if (state.atLineStart) {
        for {
          _ <- write[R](indentationChars(state))
          _ <- modifyState[R](_.copy(atLineStart = false))
        } yield ()
      } else {
        empty
      }
    }
  }

  private def indentationChars(state: PrettyPrinterState): String =
    " " * (state.indentation * state.indentationSize)


  def print[T](value: T)(implicit prettyPrinter: PrettyPrinter[T, AdditionalFx]): String = {
    val initialState = PrettyPrinterState(
      indentationSize = 4,
      indentation = 0,
      atLineStart = true
    )
    val ((), result) = runAdditionalFx(prettyPrinter.prettyPrint(value)).runWriterMonoid.evalState(initialState).run
    result
  }

  def empty: PP[Unit] =
    unit[R]

  def space: PP[Unit] =
    append(' ')

  def dollar: PP[Unit] =
    append('$')

  def newline: PP[Unit] =
    startNewLine()

  def code(str: String): PP[Unit] = {
    append(str)
  }

  def pretty[I](value: I)(implicit innerPrettyPrinter: PrettyPrinter[I, AdditionalFx]): PP[Unit] =
    innerPrettyPrinter.prettyPrint(value)

  def between[I](left: String, right: String, inner: I)(implicit innerPrettyPrinter: PrettyPrinter[I, AdditionalFx]): PP[Unit] =
    for {
      _ <- append(left)
      _ <- pretty(inner)
      _ <- append(right)
    } yield ()

  def parenthesed[I](inner: I)(implicit innerPrettyPrinter: PrettyPrinter[I, AdditionalFx]): PP[Unit] =
    between("(", ")", inner)

  def squareBracketed[I](inner: I)(implicit innerPrettyPrinter: PrettyPrinter[I, AdditionalFx]): PP[Unit] =
    between("[", "]", inner)

  def curlyBracketed[I](inner: I)(implicit innerPrettyPrinter: PrettyPrinter[I, AdditionalFx]): PP[Unit] =
    between("{", "}", inner)

  def doubleQuoted[I](inner: I)(implicit innerPrettyPrinter: PrettyPrinter[I, AdditionalFx]): PP[Unit] =
    between("\"", "\"", inner)

  def indented[A](block: Eff[R, A]): Eff[R, A] =
    for {
      _ <- indent()
      result <- block
      _ <- unindent()
    } yield result

  def sequence[I](inner: List[I], separator: String = ""): List[SeqElemBase[I]] =
    Foldable[List].intercalate(
      inner.map(elem => List(SeqElem(elem).asInstanceOf[SeqElemBase[I]])),
      List(SeqSeparator[I](separator)))

  implicit def primitivePrettyPrinter: PrettyPrinter[PP[Unit], AdditionalFx] =
    (printer: PP[Unit]) => printer

  implicit def sequencePrettyPrinter[I](implicit innerPrettyPrinter: PrettyPrinter[I, AdditionalFx]): PrettyPrinter[List[SeqElemBase[I]], AdditionalFx] =
    (elems: List[SeqElemBase[I]]) => {

      def printItems(items: List[SeqElemBase[I]]): PP[Unit] =
        items match {
          case Nil => empty
          case SeqElem(elem) :: remaining => pretty(elem) >> printItems(remaining)
          case SeqSeparator(separator) :: remaining => append(separator)  >> printItems(remaining)
        }

      printItems(elems)
    }
}
