/*
 * Copyright 2023 dragonfly.ai
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

//import slash.vector.Vec
import slash.matrix.*

class MatExt2Test extends munit.FunSuite {

  test("verify Mat scalar add is commutative") {
    val m0 = Mat[2,6](
      2,  3,  4,  5,  6,  7,
      8,  9, 10, 11, 12, 13,
    )
    val m1 = m0 + 2
    val m2 = 2.0 + m0
    assert(m1.strictEquals(m2))
  }
  test("verify Mat scalar multiply is commutative") {
    val m0 = Mat[2,6](
      2,  3,  4,  5,  6,  7,
      8,  9, 10, 11, 12, 13,
    )
    val m1 = m0 * 2.0
    val m2 = 2 * m0
    assert(m1.strictEquals(m2))
  }
  test("Can reshape matrices"){
    val m0 = Mat[2,6](
      2,  3,  4,  5,  6,  7,
      8,  9, 10, 11, 12, 13,
    )
    val expected = Mat[6,2](
       2,  3,
       4,  5,
       6,  7,
       8,  9,
      10, 11,
      12, 13,
    )
    val result = m0.reshape[6,2]
    assert(result.strictEquals(expected))
  }

  test("Mat.horzcat concatenates matrix columns"){
    val m1 = Mat[2,2](
      0,  1,
     10, 11
    )
    val m2 = Mat[2,6](
      2, 3, 4, 5, 6, 7,
     12,13,14,15,16,17
    )
    val m3 = Mat[2,1](
      8,
      18
    )
    assert(m1.rows == m2.rows && m1.rows == m3.rows)
    val all = Mat.horzcat(m1, m2, m3)

    val expected = Mat[2,9](
      0,  1,  2,  3,  4,  5,  6,  7,  8,
     10, 11, 12, 13, 14, 15, 16, 17, 18,
    )
    if (!all.strictEquals(expected)) {
      printf("all\n[%s]\n", all.toString.trim)
      printf("expected\n[%s]\n", expected.toString.trim)
    }
    assert(all.strictEquals(expected))
  }

  test("Mat.vertcat concatenates matrix rows"){
    val m1 = Mat[2,3](
       0,  1,  2,
      10, 11, 12,
    )
    val m2 = Mat[4,3](
      20, 21, 22,
      30, 31, 32,
      40, 41, 42,
      50, 51, 52,
    )
    val m3 = Mat[1,3](
      60, 61, 62,
    )
    assert(m1.columns == m2.columns && m1.columns == m3.columns)
    val all = Mat.vertcat(m1, m2, m3)

    val expected = Mat[7,3](
       0,  1,  2,
      10, 11, 12,
      20, 21, 22,
      30, 31, 32,
      40, 41, 42,
      50, 51, 52,
      60, 61, 62,
    )
    if (!all.strictEquals(expected)) {
      printf("all\n[%s]\n", all.toString.trim)
      printf("expected\n[%s]\n", expected.toString.trim)
    }
    assert(all.strictEquals(expected))
  }

}
