package scalus.merkle_patricia_forestry

import munit.diff.DiffOptions
import munit.diff.console.AnsiColors
import munit.{Assertions, Location}
import scalus.*
import scalus.uplc.Constant.toValue
import scalus.uplc.Term
import scalus.uplc.eval.{PlutusVM, Result}

import scala.quoted.*

object Macros:
    inline def assertEval(inline code: Boolean)(using vm: PlutusVM, loc: Location, diffOptions: DiffOptions): Unit =
        ${ assertEvalImpl('code, 'vm, 'loc, 'diffOptions) }

    inline def assertEqEval[A](inline actual: A, inline expected: A)(using
        vm: PlutusVM,
        loc: Location,
        diffOptions: DiffOptions
    ): Unit =
        ${ assertEqEvalImpl('actual, 'expected, 'vm, 'loc, 'diffOptions) }

    private def assertEvalImpl(
        code: Expr[Boolean],
        vm: Expr[PlutusVM],
        loc: Expr[Location],
        diffOptions: Expr[DiffOptions]
    )(using Quotes): Expr[Unit] =
        '{
            Assertions.assert($code)
            val uplc = Compiler.compile($code).toUplc(generateErrorTraces = true)
            uplc.evaluateDebug(using $vm) match
                case r: Result.Success => ()
                case r: Result.Failure => Assertions.fail("Failed", r.exception)(using $loc, $diffOptions)
        }

    private def assertEqEvalImpl[A: Type](
        actual: Expr[A],
        expected: Expr[A],
        vm: Expr[PlutusVM],
        loc: Expr[Location],
        diffOptions: Expr[DiffOptions]
    )(using Quotes): Expr[Unit] =
        '{
            Assertions.assertEquals[A, A]($actual, $expected)
            val uplc = Compiler.compile($actual).toUplc(generateErrorTraces = true)
            uplc.evaluateDebug(using $vm) match
                case Result.Success(Term.Const(c), budget, _, _) =>
                    Assertions.assertEquals(toValue(c), $expected)
                    val log = AnsiColors.c(s"BUDGET: ${budget.showJson}", AnsiColors.GREEN)
                    println(log)
                case Result.Success(t, _, _, _) =>
                    Assertions.fail(s"Expected ${$expected}, but got UPLC term ${t.show}")(using
                      $loc,
                      $diffOptions
                    )
                case r: Result.Failure => Assertions.fail("Failed", r.exception)(using $loc, $diffOptions)
        }
