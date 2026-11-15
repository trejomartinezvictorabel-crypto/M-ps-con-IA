package mx.campnav.offline.data

import mx.campnav.offline.database.AppDatabase
import mx.campnav.offline.database.PoiEntity
import mx.campnav.offline.database.RouteEntity
import mx.campnav.offline.database.HistoryEntity

class NavigationRepository(private val database: AppDatabase) {
    private val dao = database.navigationDao()
    val pois = dao.observePois()
    val routes = dao.observeRoutes()
    val history = dao.observeHistory()

    suspend fun savePoi(item: PoiEntity) = dao.insertPoi(item)
    suspend fun saveRoute(item: RouteEntity) = dao.insertRoute(item)
    suspend fun saveHistory(item: HistoryEntity) = dao.insertHistory(item)
    suspend fun deletePoi(id: Long) = dao.deletePoi(id)
}
