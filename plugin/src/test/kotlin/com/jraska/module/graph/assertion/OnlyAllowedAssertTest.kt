package com.jraska.module.graph.assertion

import com.jraska.module.graph.DependencyGraph
import org.gradle.api.GradleException
import org.junit.Test

class OnlyAllowedAssertTest {
  @Test(expected = GradleException::class)
  fun failsWithNoMatchingMatchers() {
    val dependencyGraph = testGraph()

    OnlyAllowedAssert(emptyArray()).assert(dependencyGraph)
  }

  @Test
  fun passesWhenAllAllowed() {
    val dependencyGraph = testGraph()

    OnlyAllowedAssert(arrayOf(".* -> .*")).assert(dependencyGraph)
  }

  @Test
  fun passesWhenAllowed() {
    val dependencyGraph = testGraph()

    val allowedDependencies = arrayOf(
      "app -> .*",
      "feature[a-z]* -> lib[0-9]*",
      "feature[a-z]* -> api[0-9]*",
      "api[0-9]* -> lib",
    )

    OnlyAllowedAssert(allowedDependencies).assert(dependencyGraph)
  }

  @Test(expected = GradleException::class)
  fun failsWhenOneNotAllowed() {
    val dependencies = testGraph().dependencyPairs().toMutableList().apply { add("api" to "lib2") }
    val dependencyGraph = DependencyGraph.create(dependencies)

    val allowedDependencies = arrayOf(
      "app -> .*",
      "feature[a-z]* -> lib[0-9]*",
      "feature[a-z]* -> api[0-9]*",
      "api[0-9]* -> lib",
    )

    OnlyAllowedAssert(allowedDependencies).assert(dependencyGraph)
  }

  @Test
  fun passesWhenAllowedWithAlias() {
    val dependencyGraph = testGraph()

    val allowedDependencies = arrayOf(
      "App -> .*",
      "Impl -> Api",
      "Api -> Api"
    )
    val aliases = mapOf(
      "app" to "App",
      "feature" to "Impl",
      "feature2" to "Impl",
      "api" to "Api",
      "api2" to "Api",
      "lib" to "Api",
      "lib2" to "Api",
    )

    OnlyAllowedAssert(allowedDependencies, aliases).assert(dependencyGraph)
  }

  @Test
  fun passesWhenViolationIsAllowed() {
    val dependencies = testGraph().dependencyPairs().toMutableList().apply { add("api" to "lib2") }
    val dependencyGraph = DependencyGraph.create(dependencies)

    val allowedDependencies = arrayOf(
      "app -> .*",
      "feature[a-z]* -> lib[0-9]*",
      "feature[a-z]* -> api[0-9]*",
      "api[0-9]* -> lib",
    )

    val allowedViolations = mapOf(
      "api" to listOf("lib2")
    )

    OnlyAllowedAssert(allowedDependencies, allowedViolations = allowedViolations).assert(dependencyGraph)
  }

  @Test
  fun passesWhenMultipleViolationsAreAllowed() {
    val dependencies = testGraph().dependencyPairs().toMutableList().apply { 
      add("api" to "lib2")
      add("feature" to "lib")
    }
    val dependencyGraph = DependencyGraph.create(dependencies)

    val allowedDependencies = arrayOf(
      "app -> .*",
      "feature[a-z]* -> api[0-9]*",
      "api[0-9]* -> lib",
    )

    val allowedViolations = mapOf(
      "api" to listOf("lib2"),
      "feature" to listOf("lib")
    )

    OnlyAllowedAssert(allowedDependencies, allowedViolations = allowedViolations).assert(dependencyGraph)
  }

  private fun testGraph(): DependencyGraph {
    return DependencyGraph.create(
      "app" to "feature",
      "app" to "feature2",
      "app" to "api",
      "feature" to "api",
      "feature" to "api2",
      "api" to "lib",
      "api2" to "lib",
    )
  }
}
