package io.github.vigoo.simpp

import org.atnos.eff.Eff

trait PrettyPrinter[-T, AdditionalFx] {
  def prettyPrint(value: T): Eff[PrettyPrint.PrettyPrinterContext[AdditionalFx], Unit]
}
