package ru.hse.spb

import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

open class SimpleGraph {
    private val vertices: MutableSet<Int> = HashSet()
    private val edges: MutableMap<Int, MutableSet<Int>> = HashMap()

    fun addEdge(v1: Int, v2: Int) {
        if (vertices.add(v1)) {
            edges[v1] = HashSet()
        }
        if (vertices.add(v2)) {
            edges[v2] = HashSet()
        }
        edges[v1]!!.add(v2)
        edges[v2]!!.add(v1)
    }

    private fun getInitialVerticesStates(): MutableMap<Int, VertexState> {
        val verticesStates: MutableMap<Int, VertexState> = HashMap()
        vertices.forEach(Consumer { vertex -> verticesStates[vertex] = VertexState.NOT_PROCESSED })
        return verticesStates
    }

    fun findAnyCycle(): Optional<List<Int>> {
        return if (vertices.isEmpty()) Optional.empty()
        else findAnyCycleRecursively(vertices.first(), getInitialVerticesStates(), HashMap())
    }

    private fun findAnyCycleRecursively(currentVertex: Int,
                                        verticesStates: MutableMap<Int, VertexState>,
                                        parents: MutableMap<Int, Int>): Optional<List<Int>> {
        verticesStates[currentVertex] = VertexState.PROCESSING
        for (neighbour in edges[currentVertex]!!) {
            if (neighbour == parents[currentVertex]) {
                continue
            }
            if (verticesStates[neighbour] == VertexState.NOT_PROCESSED) {
                parents[neighbour] = currentVertex
                val cycle = findAnyCycleRecursively(neighbour, verticesStates, parents)
                if (cycle.isPresent) {
                    return cycle
                }
            } else if (verticesStates[neighbour] == VertexState.PROCESSING) {
                return Optional.of(extractCycle(neighbour, currentVertex, parents))
            }
        }
        verticesStates[currentVertex] = VertexState.PROCESSED
        return Optional.empty()
    }

    private fun extractCycle(startVertex: Int,
                             endVertex: Int,
                             parents: MutableMap<Int, Int>): List<Int> {
        var currentVertex = endVertex
        val cycle: MutableList<Int> = ArrayList()
        while (currentVertex != startVertex) {
            cycle.add(currentVertex)
            currentVertex = parents[currentVertex]!!
        }
        cycle.add(startVertex)
        return cycle
    }

    fun collapseVertices(verticesToCollapse: Set<Int>): Int {
        if (verticesToCollapse.size < 2) {
            throw IllegalArgumentException("Less than 2 vertices cannot be collapsed")
        }
        for (vertex in verticesToCollapse) {
            edges[vertex]!!.removeAll { neighbour -> verticesToCollapse.contains(neighbour) }
        }
        val verticesIterator = verticesToCollapse.iterator()
        val mainVertex = verticesIterator.next()
        while (verticesIterator.hasNext()) {
            val currentVertex = verticesIterator.next()
            val neighboursOfCurrentVertex = edges[currentVertex]!!
            edges[mainVertex]!!.addAll(neighboursOfCurrentVertex)
            for (neighbour in neighboursOfCurrentVertex) {
                edges[neighbour]!!.add(mainVertex)
                edges[neighbour]!!.remove(currentVertex)
            }
            vertices.remove(currentVertex)
            edges.remove(currentVertex)
        }
        return mainVertex
    }

    fun findDistances(vertex: Int): Map<Int, Int> {
        val distances: MutableMap<Int, Int> = HashMap()
        val vertexQueue: Queue<Int> = ArrayDeque<Int>()
        val visited: MutableSet<Int> = HashSet()
        distances[vertex] = 0
        vertexQueue.add(vertex)
        visited.add(vertex)
        while (vertexQueue.isNotEmpty()) {
            val currentVertex = vertexQueue.poll()
            for (neighbour in edges[currentVertex]!!) {
                if (!visited.contains(neighbour)) {
                    vertexQueue.add(neighbour)
                    distances[neighbour] = distances[currentVertex]!! + 1
                    visited.add(neighbour)
                }
            }
        }
        return distances
    }

    enum class VertexState {
        PROCESSED, PROCESSING, NOT_PROCESSED
    }
}

fun main(args: Array<String>) {
    val graph = SimpleGraph()
    with(Scanner(System.`in`)) {
        val numberOfVertices = nextInt()
        for (i in 0 until numberOfVertices) {
            graph.addEdge(nextInt(), nextInt())
        }
    }
    for (distance in getDistancesFromCycle(graph).values) {
        print(distance.toString() + " ")
    }
}

fun getDistancesFromCycle(graph: SimpleGraph): Map<Int, Int> {
    val foundCycle = graph.findAnyCycle()
            .orElseThrow { IllegalArgumentException("Input graph must have cycle") }
    val distancesFromCycle: MutableMap<Int, Int> = HashMap()
    foundCycle.forEach(Consumer { vertexOnCycle -> distancesFromCycle[vertexOnCycle] = 0 })
    val newVertex = graph.collapseVertices(HashSet(foundCycle))
    distancesFromCycle.putAll(graph.findDistances(newVertex))
    return distancesFromCycle
}