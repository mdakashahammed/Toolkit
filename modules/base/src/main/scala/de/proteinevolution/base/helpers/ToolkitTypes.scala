/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
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

package de.proteinevolution.base.helpers

import reactivemongo.bson.{ BSONDocument, BSONElement, Producer }

import scala.concurrent.Future

trait ToolkitTypes {

  type BDOC = BSONDocument

  type Fu[A] = Future[A]

  @inline def bdoc(elements: Producer[BSONElement]*): BDOC = BSONDocument(elements: _*)

  @inline def fuccess[A](a: A): Fu[A] = Future.successful(a) // lila-style

  // scalac style
  implicit class ToFutureSuccessful[T](obj: T) {
    @inline def asFuture: Fu[T] = Future.successful(obj)
  }

}

object ToolkitTypes extends ToolkitTypes with EnumerationOps
