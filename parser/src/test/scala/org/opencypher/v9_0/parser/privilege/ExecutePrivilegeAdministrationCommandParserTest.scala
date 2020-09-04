/*
 * Copyright © 2002-2020 Neo4j Sweden AB (http://neo4j.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.v9_0.parser.privilege

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.ast.ExecuteProcedureAction
import org.opencypher.v9_0.ast.ProcedureQualifier
import org.opencypher.v9_0.expressions
import org.opencypher.v9_0.parser.AdministrationCommandParserTestBase
import org.opencypher.v9_0.util.InputPosition

class ExecutePrivilegeAdministrationCommandParserTest extends AdministrationCommandParserTestBase {
  private val starString = "*"
  private val apocString = "apoc"
  private val mathString = "math"

  Seq(
    ("GRANT", "TO", grantExecutePrivilege: executePrivilegeFunc),
    ("DENY", "TO", denyExecutePrivilege: executePrivilegeFunc),
    ("REVOKE GRANT", "FROM", revokeGrantExecutePrivilege: executePrivilegeFunc),
    ("REVOKE DENY", "FROM", revokeDenyExecutePrivilege: executePrivilegeFunc),
    ("REVOKE", "FROM", revokeExecutePrivilege: executePrivilegeFunc)
  ).foreach{
    case (verb: String, preposition: String, func: executePrivilegeFunc) =>

      Seq(
        ("EXECUTE PROCEDURE", ExecuteProcedureAction)
      ).foreach {
        case (execute, action) =>

          test(s"$verb $execute * ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(starString)), Seq(literalRole)))
          }

          test(s"$verb ${execute}S * ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(starString)), Seq(literalRole)))
          }

          test(s"$verb ${execute}S `*` ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(starString)), Seq(literalRole)))
          }

          test(s"$verb $execute apoc.procedure ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(apocString), "procedure")), Seq(literalRole)))
          }

          test(s"$verb ${execute}S apoc.procedure ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(apocString), "procedure")), Seq(literalRole)))
          }

          test(s"$verb $execute apoc.math.sin ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(apocString, mathString), "sin")), Seq(literalRole)))
          }

          test(s"$verb $execute apoc* ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier("apoc*")), Seq(literalRole)))
          }

          test(s"$verb $execute *apoc ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier("*apoc")), Seq(literalRole)))
          }

          test(s"$verb $execute apoc.*.math.* ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(apocString, starString, mathString), starString)), Seq(literalRole)))
          }

          test(s"$verb $execute math.*n ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(mathString), "*n")), Seq(literalRole)))
          }

          test(s"$verb $execute mat?.`a.\n`.*n ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List("mat?", "a.\n"), "*n")), Seq(literalRole)))
          }

          test(s"$verb $execute *.sin ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(starString), "sin")), Seq(literalRole)))
          }

          test(s"$verb $execute apoc.math.* ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(apocString, mathString), starString)), Seq(literalRole)))
          }

          test(s"$verb $execute math.sin, math.cos ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(mathString), "sin"), procedureQualifier(List(mathString), "cos")), Seq(literalRole)))
          }

          test(s"$verb $execute apoc.math.sin, math.* ON DBMS $preposition role") {
            yields(func(action, List(procedureQualifier(List(apocString, mathString), "sin"), procedureQualifier(List(mathString), starString)), Seq(literalRole)))
          }
      }
  }

  private def procedureQualifier(procName: String): InputPosition => ProcedureQualifier = procedureQualifier(List.empty, procName)

  private def procedureQualifier(nameSpace: List[String], procName: String): InputPosition => ProcedureQualifier =
    ast.ProcedureQualifier(expressions.Namespace(nameSpace)(_), expressions.ProcedureName(procName)(_))(_)
}
