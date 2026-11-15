package mx.campnav.offline.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pois", indices = [Index(value = ["latitude", "longitude"])])
data class PoiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double,
    val endLongitude: Double,
    val mode: String,
    val encodedNodeIds: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "navigation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long?,
    val distanceMeters: Double,
    val mode: String,
    val startLatitude: Double?,
    val startLongitude: Double?,
    val endLatitude: Double?,
    val endLongitude: Double?
)

@Dao
interface NavigationDao {
    @Insert suspend fun insertPoi(poi: PoiEntity): Long
    @Query("SELECT * FROM pois ORDER BY createdAt DESC") fun observePois(): Flow<List<PoiEntity>>
    @Query("DELETE FROM pois WHERE id = :id") suspend fun deletePoi(id: Long)

    @Insert suspend fun insertRoute(route: RouteEntity): Long
    @Query("SELECT * FROM saved_routes ORDER BY createdAt DESC") fun observeRoutes(): Flow<List<RouteEntity>>

    @Insert suspend fun insertHistory(item: HistoryEntity): Long
    @Query("SELECT * FROM navigation_history ORDER BY startedAt DESC LIMIT 100") fun observeHistory(): Flow<List<HistoryEntity>>
}

@Database(
    entities = [PoiEntity::class, RouteEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun navigationDao(): NavigationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campo_nav.db"
                ).build().also { INSTANCE = it }
            }
    }
}
