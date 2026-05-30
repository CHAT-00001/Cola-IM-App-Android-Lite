package com.cola.im.sdk.storage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cola.im.sdk.storage.dao.MessageDao
import com.cola.im.sdk.storage.entity.MessageEntity
import timber.log.Timber
import java.security.MessageDigest

/**
 * Room 数据库——单用户单库隔离
 *
 * 数据库文件命名：cola_im_{sha256(uid)}.db
 * 通过 Callback 开启 WAL 模式
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

        private val WAL_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.enableWriteAheadLogging()
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.enableWriteAheadLogging()
            }
        }

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
                    .addCallback(WAL_CALLBACK)
                    .build()

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