package io.github.vigoo.simpp

import org.atnos.eff.{Eff, NoFx}
import org.specs2._
import PrettyPrint.PrettyPrinterContext

object BasePrettyPrinter extends PrettyPrint[NoFx] {
  override def runAdditionalFx(f: Eff[BasePrettyPrinter.R, Unit]): Eff[PrettyPrinterContext[NoFx], Unit] = f
}

class PrettyPrinterSpecs extends SpecificationWithJUnit with PrettyPrinterTests[NoFx, BasePrettyPrinter.type] { def is = s2"""
  With the pretty printer
    empty prints nothing            $emptyTest
    space prints space              $spaceTest
    dollar prints dollar            $dollarTest
    newline prints \\n              $newlineTest
    code prints the arg             $codeTest
    between wraps an inner printer  $betweenTest
    parenthesed wraps in ()         $parenthesedTest
    squareBracketed wraps in []     $squareBracketedTest
    curlyBracketed wraps in {}      $curlyBracketedTest
    doubleQuoted wraps in ""        $doubleQuotedTest
    flatMap concatenates            $flatMapTest
    indentation works               $indentedTest
    sequence without sep works      $seqWithoutSep
    sequence with sep works         $seqWithSep
    sequence with newline in sep    $seqWithSepNewLine
    appending strings with newlines $appendMultiLine
    appending newline char works    $appendNewLineChar
    appending string ending newline $appendStringEndingNewLine
  """

  override val pp = BasePrettyPrinter
  import pp._

  def emptyTest = pp.empty should bePrinting("")
  def spaceTest = space should bePrinting(" ")
  def dollarTest = dollar should bePrinting("$")
  def newlineTest = newline should bePrinting("\n")
  def codeTest = code("hello world") should bePrinting("hello world")
  def betweenTest = pp.between("left", "right", code("inner")) should bePrinting("leftinnerright")
  def parenthesedTest = parenthesed(dollar) should bePrinting("($)")
  def squareBracketedTest = squareBracketed(dollar) should bePrinting("[$]")
  def curlyBracketedTest = curlyBracketed(dollar) should bePrinting("{$}")
  def doubleQuotedTest = doubleQuoted(code("hello world")) should bePrinting("\"hello world\"")
  def flatMapTest = (code("hello") >> space >> code("world")) should bePrinting("hello world")
  def indentedTest =
    (code("first line") >> newline >> indented(code("second line") >> newline >> code("third line") >> newline) >> code("fourth line")).should(
      bePrinting(
        """first line
          |    second line
          |    third line
          |fourth line""".stripMargin
      ))

  def seqWithoutSep = sequence(List(code("1"), code("2"), code("3"))) should bePrintedAs("123")
  def seqWithSep = sequence(List(code("1"), code("2"), code("3")), ", ") should bePrintedAs("1, 2, 3")
  def seqWithSepNewLine = sequence(List(code("1"), code("2"), code("3")), ",\n") should bePrintedAs("1,\n2,\n3")

  def appendMultiLine = indented(append("first line\nsecond line")) >> newline >> code("third line") should bePrinting("    first line\n    second line\nthird line")

  def appendNewLineChar = indented(append("xyz") >> append('\n') >> append("123")) should bePrinting("    xyz\n    123")

  def appendStringEndingNewLine = (append("x\n") >> append("y")) should bePrinting("x\ny")
}
