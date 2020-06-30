/*
 * Copyright (c) 2002-2018 "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * This file is part of ONgDB Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) as found
 * in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 */
package org.neo4j.cypher.internal.runtime.compiled.codegen.ir.expressions

import org.neo4j.cypher.internal.runtime.compiled.codegen.CodeGenContext
import org.neo4j.cypher.internal.runtime.compiled.codegen.spi.MethodStructure
import org.neo4j.cypher.internal.v4_0.util.symbols.CTBoolean

case class Or(lhs: CodeGenExpression, rhs: CodeGenExpression) extends CodeGenExpression {

  override final def init[E](generator: MethodStructure[E])(implicit context: CodeGenContext) = {
    lhs.init(generator)
    rhs.init(generator)
  }

  override def nullable(implicit context: CodeGenContext) = lhs.nullable || rhs.nullable

  override def codeGenType(implicit context: CodeGenContext) =
    if (!nullable) {
      CodeGenType.primitiveBool
    } else {
      CypherCodeGenType(CTBoolean, ReferenceType)
    }

  override def generateExpression[E](structure: MethodStructure[E])(implicit context: CodeGenContext): E =
    if (!nullable) {
      (lhs.codeGenType, rhs.codeGenType) match {
        case (t1, t2) if t1.isPrimitive && t2.isPrimitive =>
          structure.orExpression(lhs.generateExpression(structure), rhs.generateExpression(structure))
        case (t1, t2) if t1.isPrimitive =>
          structure.orExpression(lhs.generateExpression(structure), structure.unbox(rhs.generateExpression(structure), t2))
        case (t1, t2) if t2.isPrimitive =>
          structure.orExpression(structure.unbox(lhs.generateExpression(structure), t1), rhs.generateExpression(structure))
        case _ =>
          structure.unbox(
            structure.threeValuedOrExpression(structure.box(lhs.generateExpression(structure), lhs.codeGenType), structure.box(rhs.generateExpression(structure), rhs.codeGenType)),
            CypherCodeGenType(CTBoolean, ReferenceType))
      }
    } else {
      structure.threeValuedOrExpression(structure.box(lhs.generateExpression(structure), lhs.codeGenType),
        structure.box(rhs.generateExpression(structure), rhs.codeGenType))
    }
}
