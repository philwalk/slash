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

package slash

import narr.*
import slash.vector.{Vec, *}
import slash.matrix.Mat.{Number, toDouble}
import slash.matrix.decomposition.{SV, *}

import scala.compiletime.ops.int.*
import scala.compiletime.ops.any.==
import scala.compiletime.ops.boolean.||

package object matrix {

  extension[N <: Int] (thisVector: Vec[N])(using ValueOf[N]) {
    inline def asRowMatrix: Mat[1, N] = Mat[1, N](thisVector.asInstanceOf[NArray[Double]])
    inline def asColumnMatrix: Mat[N, 1] = Mat[N, 1](thisVector.asInstanceOf[NArray[Double]])

    def times [M <: Int](thatMatrix: Mat[N, M])(using ValueOf[M]): Mat[1, M] = asRowMatrix * thatMatrix
    inline def * [M <: Int](thatMatrix: Mat[N, M])(using ValueOf[M]): Mat[1, M] = times(thatMatrix)
  }

  /**
   * Extension Methods to support right addition and multiplication by scalars.
   */
  extension(s: Double) {
    // to support s + m
    inline def +[M <: Int, N <: Int](inline m: Mat[M,N])(using ValueOf[M], ValueOf[N]): Mat[M,N] = m.copy.addScalar(s)
    inline def +=[M <: Int, N <: Int](inline m: Mat[M,N])(using ValueOf[M], ValueOf[N]): Mat[M,N] = m.addScalar(s)
    // to support s * m
    inline def *[M <: Int, N <: Int](inline m: Mat[M,N])(using ValueOf[M], ValueOf[N]): Mat[M,N] = m.copy.times(s)
    inline def *=[M <: Int, N <: Int](inline m: Mat[M,N])(using ValueOf[M], ValueOf[N]): Mat[M,N] = m.times(s)
  }
  extension(s: Int) {
    // to support s + m
    inline def +[M <: Int, N <: Int](inline m: Mat[M,N])(using ValueOf[M], ValueOf[N]): Mat[M,N] = m.copy.addScalar(s.toDouble)
    inline def +=[M <: Int, N <: Int](inline m: Mat[M,N])(using ValueOf[M], ValueOf[N]): Mat[M,N] = m.addScalar(s.toDouble)
    // to support s * m
    inline def *[M <: Int, N <: Int](inline m: Mat[M,N])(using ValueOf[M], ValueOf[N]): Mat[M,N] = m.copy.times(s.toDouble)
    inline def *=[M <: Int, N <: Int](inline m: Mat[M,N])(using ValueOf[M], ValueOf[N]): Mat[M,N] = m.times(s.toDouble)
  }

  /**
   * Extension Methods for Square Matrices.
   */
  extension [MN <: Int](m: Mat[MN, MN])(using ValueOf[MN]) {
    /**
     * https://en.wikipedia.org/wiki/Invertible_matrix
     *
     * Computes the inverse of Square Mat m.
     * @throws RuntimeException( "Mat is singular." )
     * @return the inverse of matrix m
     */
    def inverse: Mat[MN, MN] = solve(Mat.identity[MN, MN])

    /** Solve a * x = b
     *
     * @param b right hand side
     * @return x = Mat[MN, V] such that a * x = b
     */
    def solve[V <: Int](b: Mat[MN, V])(using ValueOf[V]): Mat[MN, V] = LU[MN, MN](m).solve(b)

    /** Mat determinant
     * https://en.wikipedia.org/wiki/Determinant
     * the determinant is nonzero if and only if the matrix is invertible and the linear map represented by the matrix is an isomorphism
     * @return the determinant of this matrix.
     */
    def determinant: Double = LU[MN, MN](m).determinant

    
  }


  /**
   * Extension methods for all matrices.
   */
  extension[M <: Int, N <: Int](a: Mat[M, N])(using ValueOf[M], ValueOf[N]) {
    /** element-wise matrix add by a scalar
    *
    * @param s scalar
    * @return A+s
    */
    inline def + (s: Number): Mat[M,N] = a.copy.addScalar(toDouble(s))

    /** A = A + d
    *
    * @param d a scalar
    * @return A + d
    */
    def addScalar(num: Number)(using ValueOf[N]): Mat[M,N] = {
      val d = toDouble(num)
      for(i <- 0 until a.rows){
        for(j <- 0 until a.columns){
          a(i,j) += d
        }
      }
      a
    }

    def reshape[R <: Int, C <: Int](using ValueOf[R], ValueOf[C]): Mat[R,C] = {
      val (msize,nsize) = (valueOf[M], valueOf[N])
      val (r,c) = (valueOf[R], valueOf[C])
      require(msize*nsize == r*c, s"$msize x $nsize != $r x $c")
      val nums = for {
        i <- 0 until msize
        j <- 0 until nsize
      } yield a(i,j)
      new Mat[R,C](NArray(nums *))
    }

    def upper: Mat[M,N] = {
      val nums = for {
        i <- 0 until a.rows
        j <- 0 until a.columns
        e: Double = if (i>j) 0.0 else a(i,j)
      } yield e
      new Mat[M,N](NArray(nums *))
    }

    def lower: Mat[M,N] = {
      val nums = for {
        i <- 0 until a.rows
        j <- 0 until a.columns
        e: Double = if (i<j) 0.0 else a(i,j)
      } yield e
      new Mat[M,N](NArray(nums *))
    }

    inline def diagvec: Vec[Min[M,N]] = {
      val dim: Int = valueOf[M].min(valueOf[N])
      val nums = for {
        i <- 0 until a.rows
        j <- 0 until a.columns
        if i == j
      } yield a(i,j)
      Vec[Min[M,N]](nums *)
    }
  }

  /**
   * Extension methods for rectangular matrices.
   */
  extension[M <: Int, N <: Int](a: Mat[M, N])(using ValueOf[M], ValueOf[N], ValueOf[Min[M, N]], (N =:= M) =:= false) {
    /** Solve a * x = b
     *
     * @param b right hand side
     * @return least squares solution x = Mat[M, V] such that a * x = b
     */
    def solve[V <: Int](b: Mat[M, V])(using ValueOf[V]): Mat[N, V] = QR[M, N](a).solve(b)

  }

  /**
   * Extension methods for rectangular matrices where M > N.
   */
  extension[M <: Int, N <: Int](m: Mat[M, N])(using ValueOf[M], ValueOf[N], ValueOf[Min[M, N]], M >= N =:= true) {
    /** Solve b * m = I[N, N]
     * m = Mat[M, N] with M > N and Rank = N, has a left inverse b = Mat[N, M] such that b * m = I[N, N]
     * @return b = Mat[N, M] the Left Inverse of Mat m.
     */
    def leftInverse: Mat[N, M] = {
      val svd = SV[M, N](m)
      svd.V * svd.S_inverse * svd.U.transpose
    }


    /** Two norm
     *
     * @return maximum singular value.
     */
    def norm2: Double = SV[M, N](m).norm2

    /** Mat rank
     *
     * @return effective numerical rank, obtained from SV.
     */
    def rank: Int = SV[M, N](m).rank

    /** Mat condition (2 norm)
     *
     * @return ratio of largest to smallest singular value.
     */
    def cond: Double = SV[M, N](m).cond

  }

  /**
   * Extension methods for rectangular matrices where M < N.
   */
  extension[M <: Int, N <: Int](m: Mat[M, N])(using ValueOf[M], ValueOf[N], ValueOf[Min[M, N]], N > M =:= true) {
    /**
     * m = Mat[M, N] with M < N and Rank = M, has a right inverse b = Mat[N, M] such that m * b = Identity[M, M]
     * @return the Right Inverse of Mat a.
     */
    def rightInverse(using ValueOf[Min[M, M]]): Mat[N, M] = QR[M, N](m).solve(Mat.identity[M, M])

  }

  extension[M <: Int, N <: Int](m: Mat[M, N])(using (M == 1 || N == 1) =:= true) {
    def asVector: Vec[M*N] = m.values.asInstanceOf[Vec[M*N]]
    inline def copyAsVector[MN <: Int](using MN == (M * N) =:= true): Vec[MN] = narr.copy[Double](m.values).asInstanceOf[Vec[MN]]
  }

  /*
  type MinDim[M <: Int, N <: Int] = M < N match
    case true  => M // If A < B, the minimum is A
    case false => N // Otherwise, the minimum is B
    */

}
