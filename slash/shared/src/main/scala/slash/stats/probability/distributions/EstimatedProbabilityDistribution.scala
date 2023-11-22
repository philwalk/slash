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

package slash.stats.probability.distributions

import scala.reflect.ClassTag

trait EstimatedProbabilityDistribution[DOMAIN:ClassTag, PPD <: ParametricProbabilityDistribution[DOMAIN]] extends ProbabilityDistribution[DOMAIN] with SampledPointStatistics[DOMAIN] {

  val idealized: PPD

  override inline def sampleMean: Double = idealized.mean
  override inline def sampleVariance: Double = idealized.variance

  inline def p(x: DOMAIN): Double = idealized.p(x)

  override inline def random(r:scala.util.Random = slash.Random.defaultRandom): DOMAIN = idealized.random()
}