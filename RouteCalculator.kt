package mx.campnav.offline.navigation

import kotlin.math.*

data class RoadNode(val id: Long, val latitude: Double, val longitude: Double)
data class RoadEdge(
    val fromNodeId: Long,
    val toNodeId: Long,
    val distanceMeters: Double,
    val allowedModes: Set<TransportMode>,
    val bidirectional: Boolean = true
)
data class CalculatedRoute(val nodeIds: List<Long>, val distanceMeters: Double)

object RouteCalculator {
    // A* sobre grafo local. Los nodos/aristas deben importarse previamente desde datos OSM.
    fun findRoute(
        startId: Long,
        goalId: Long,
        nodes: Map<Long, RoadNode>,
        edges: List<RoadEdge>,
        mode: TransportMode
    ): CalculatedRoute? {
        if (startId !in nodes || goalId !in nodes) return null
        val adjacency = HashMap<Long, MutableList<RoadEdge>>()
        edges.asSequence().filter { mode in it.allowedModes && it.distanceMeters > 0.0 }.forEach { edge ->
            adjacency.getOrPut(edge.fromNodeId) { mutableListOf() }.add(edge)
            if (edge.bidirectional) {
                adjacency.getOrPut(edge.toNodeId) { mutableListOf() }
                    .add(edge.copy(fromNodeId = edge.toNodeId, toNodeId = edge.fromNodeId))
            }
        }

        fun heuristic(a: RoadNode, b: RoadNode) = haversine(a.latitude, a.longitude, b.latitude, b.longitude)
        val open = java.util.PriorityQueue<Pair<Long, Double>>(compareBy { it.second })
        val cameFrom = HashMap<Long, Long>()
        val gScore = HashMap<Long, Double>().withDefault { Double.POSITIVE_INFINITY }
        gScore[startId] = 0.0
        open.add(startId to heuristic(nodes.getValue(startId), nodes.getValue(goalId)))

        while (open.isNotEmpty()) {
            val current = open.poll().first
            if (current == goalId) {
                val path = mutableListOf(current)
                var cursor = current
                while (cursor in cameFrom) {
                    cursor = cameFrom.getValue(cursor)
                    path += cursor
                }
                path.reverse()
                return CalculatedRoute(path, gScore.getValue(goalId))
            }
            for (edge in adjacency[current].orEmpty()) {
                val tentative = gScore.getValue(current) + edge.distanceMeters
                if (tentative < gScore.getValue(edge.toNodeId)) {
                    cameFrom[edge.toNodeId] = current
                    gScore[edge.toNodeId] = tentative
                    val f = tentative + heuristic(nodes.getValue(edge.toNodeId), nodes.getValue(goalId))
                    open.add(edge.toNodeId to f)
                }
            }
        }
        return null
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}
