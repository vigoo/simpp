# simpp
[![Build Status](https://travis-ci.org/vigoo/simpp.svg?branch=master)](https://travis-ci.org/vigoo/simpp)
[![codecov](https://codecov.io/gh/vigoo/simpp/branch/master/graph/badge.svg)](https://codecov.io/gh/vigoo/simpp)
[![Apache 2 License License](http://img.shields.io/badge/license-APACHE2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

A *simple pretty printer* library primarily targeting source code generation.

Built on top of [eff](http://atnos-org.github.io/eff/).


## Usage

Add the following dependency:

```scala
libraryDependencies += "io.github.vigoo" %% "simpp" % "0.2.0"
```

Use the `PrettyPrint[AdditionalFx]` trait as a context to define the pretty printer. This defines the pretty printer
context `PrettyPrinterContext` which is a combination of a writer and state effect plus the given effects specified in
the `AdditionalFx` parameter. The pretty printer functions must have type `PP[A]` which is in fact 
`Eff[PrettyPrinterContext[AdditionalFx], A]`. 

You have to define the `runAdditionalFx` method to specify how the custom effects are to be interpreted.

In the simple case there are no additional effects:

```scala
import io.github.vigoo.simpp._
import org.atnos.eff.{Eff, NoFx}

object SamplePrettyPrint extends PrettyPrint[NoFx] {
  override def runAdditionalFx(f: PP[Unit]): Eff[PrettyPrinterContext[NoFx], Unit] = f
 
  // Define pretty printing functions here
  
  // Define implicit pretty printers here 
}
``` 

Then you will have a `SamplePrettyPrint.print` method for any value of type `T` with an implicit 
`PrettyPrinter[T, AdditionalFx]` instance.

The idea is to:
- Specify pretty printing functions of type `PP[A]`. There is a couple of basic ones available in the `PrettyPrint` 
trait.
- Specify implicit instances of the `PrettyPrinter` type class
- Use `SamplePrettyPrint.print` to render values to strings

### Pretty printing functions
Pretty printing functions can be composed from more atomic pretty printing functions. The basic ones are the following:

- `empty: PP[Unit]`
- `append(value: String): PP[Unit]`
- `indent(): PP[Unit]`
- `unindent(): PP[Unit]`
- `pretty[I](value: I)(implicit prettyPrinter: PrettyPrinter[I])`

There are additional functions to wrap values, render sequences, indent blocks, etc.  

### Pretty printer instances
The `PrettyPrinter` type class provides a single method `prettyPrint` that can be defined by composing the above
defined *pretty printer functions*.

For example:

```scala
implicit val expressionPrettyPrinter: PrettyPrinter[BcExpression, NoFx] = {
  case BcExpressions.Number(value) => code(value.toString)
  case BcExpressions.Add(x, y) => parenthesed(x) >> space >> code("+") >> space >> parenthesed(y)
  case BcExpressions.Sub(x, y) => parenthesed(x) >> space >> code("-") >> space >> parenthesed(y)
  case BcExpressions.Mul(x, y) => parenthesed(x) >> space >> code("*") >> space >> parenthesed(y)
  case BcExpressions.Div(x, y) => parenthesed(x) >> space >> code("/") >> space >> parenthesed(y)
}
```

### Additional effects
Additional effects such as custom state can be applied to the pretty printers `AdditionalFx` type parameter.

For example the code below keeps track whether we are inside a string or not:

```scala
case class BashPrettyPrinterState(inString: Boolean)

object BashPrettyPrint extends PrettyPrint[Fx.fx1[State[BashPrettyPrinterState, ?]]] {
  override def runAdditionalFx(f: PP[Unit]): Eff[PrettyPrinterContext[NoFx], Unit] =
    f.evalState(BashPrettyPrinterState(inString = false))

  type BashFx = Fx.fx1[State[BashPrettyPrinterState, ?]]
  
  def getBashState: Eff[R, BashPrettyPrinterState] =
    get[R, BashPrettyPrinterState]

  def setBashState(newState: BashPrettyPrinterState): Eff[R, Unit] =
    put[R, BashPrettyPrinterState](newState)

  def inString[A](inner: Eff[R, A]): Eff[R, A] =
    for {
      state <- getBashState
      _ <- setBashState(state.copy(inString = true))
      result <- inner
      _ <- setBashState(state.copy(inString = state.inString))
    } yield result

  // ...
}
```
