package com.cola.im.sdk.storage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.cola.im.sdk.storage.dao.MessageDao
import com.cola.im.sdk.storage.entity.MessageEntity
import timber.log.Timber
import java.security.MessageDigest

/**
 * Room 数据库——单用户单库隔离
 *
 * 数据库文件命名：cola_im_{sha256(uid)}.db
 * 强制 WAL 模式
 * 预留 SQLCipher 加密接口（SupportSQLiteOpenHelper）
 */
@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        private const val DB_PREFIX = "cola_im_"
        private const val DB_SUFFIX = ".db"

        @Volatile
        private var instances = HashMap<String, AppDatabase>()

        /**
         * 根据 uid 获取/创建独立数据库实例
         *
         * @param context Application Context
         * @param uid 用户 ID（SHA-256 哈希后作为文件名）
         */
        fun getInstance(context: Context, uid: String): AppDatabase {
            val dbName = buildDbName(uid)

            instances[dbName]?.let { return it }

            synchronized(this) {
                instances[dbName]?.let { return it }

                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                    .openHelperFactory(object : SupportSQLiteOpenHelper.Factory {
                        override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
                            // 预留：此处可替换为 SQLCipher encrypted helper
                            return Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                dbName
                            ).build().openHelper
                        }
                    })
                    .build()

                // 强制开启 WAL 模式
                db.setQueryExecutor { Runnable {
                    db.openHelper.writableDatabase.enableWriteAheadLogging()
                    Timber.d("WAL enabled for $dbName")
                } }

                instances[dbName] = db
                Timber.i("Database opened: $dbName (uid=$uid)")
                return db
            }
        }

        /**
         * 关闭指定用户的数据库并释放资源
         */
        fun closeDatabase(uid: String) {
            val dbName = buildDbName(uid)
            instances.remove(dbName)?.close()
            Timber.i("Database closed: $dbName")
        }

        private fun buildDbName(uid: String): String {
            val hash = MessageDigest.getInstance("SHA-256")
                .digest(uid.toByteArray())
                .joinToString("") { "%02x".format(it) }
            return "${DB_PREFIX}${hash}${DB_SUFFIX}"
        }
    }
}