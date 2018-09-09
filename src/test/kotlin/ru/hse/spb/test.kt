package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test

class TestSource {
    @Test
    fun testOnlyCycle() {
        val correctAnswer: Map<Int, Int> = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0)
        val graph = SimpleGraph()
        graph.addEdge(1, 3)
        graph.addEdge(4, 3)
        graph.addEdge(4, 2)
        graph.addEdge(1, 2)
        assertEquals(correctAnswer, getDistancesFromCycle(graph))
    }

    @Test
    fun testComplex() {
        val correctAnswer: Map<Int, Int> = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 1, 5 to 1, 6 to 2)
        val graph = SimpleGraph()
        graph.addEdge(1, 2)
        graph.addEdge(3, 4)
        graph.addEdge(6, 4)
        graph.addEdge(2, 3)
        graph.addEdge(1, 3)
        graph.addEdge(3, 5)
        assertEquals(correctAnswer, getDistancesFromCycle(graph))
    }
}