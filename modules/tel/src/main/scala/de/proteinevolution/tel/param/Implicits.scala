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

package de.proteinevolution.tel.param

object Implicits {

  implicit class StringIterator(s: Iterator[String]) {

    // Changes the Strings generated by the iterator such that comments are
    // ignored. We distinguish two types:
    //
    //  "foobar # comment"
    //      -> results in "foobar"
    // " # comment"
    //      -> results in ""
    //
    // I'm just fooling, these are of course not two distinct types, I just want to make clear that comment-lines
    // resultpanel in empty Strings in the iterator and not in removing the string from the iterator!
    def withoutComment(c: Char): Iterator[String] = s.map(_.split(c)(0))

    // Removed all Lines that just consist of whitespace
    def noWSLines: Iterator[String] = s.withFilter(!_.trim().isEmpty)
  }
}
