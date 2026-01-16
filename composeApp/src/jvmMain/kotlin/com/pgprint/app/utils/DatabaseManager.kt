package com.pgprint.app.utils
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.pgprint.app.BuildConfig.STORED_DIR
import com.pgprint.app.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object DatabaseManager {
    private const val DB_NAME = "pgprint.db"

    private const val DB_VERSION = 3 // 当前数据库版本

    val dbScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob()
    )

    private fun getDatabaseFile(): File {
        // 获取应用数据目录 (使用你 BuildConfig 中定义的常量)
        val userHome = System.getProperty("user.home")
        val os = System.getProperty("os.name").lowercase()
        val folder = when {
            os.contains("win") -> {
                // 获取根盘符
                val rootDisk = File(userHome).toPath().root.toString()
                val base = if (rootDisk.startsWith("c", ignoreCase = true)) userHome else rootDisk
                File(base, "$STORED_DIR/db")
            }
            os.contains("mac") -> File(userHome, "Library/Application Support/$STORED_DIR/db")
            else -> File(userHome, ".$STORED_DIR/db") // Linux/Unix
        }
        if (!folder.exists()) folder.mkdirs()
        return File(folder, DB_NAME)
    }

    private fun setUserVersion(driver: SqlDriver, version: Int) {
        driver.execute(null, "PRAGMA user_version = $version", 0)
    }

    val database: AppDatabase by lazy (LazyThreadSafetyMode.SYNCHRONIZED) {

        val dbFile = getDatabaseFile()
        // 创建 JDBC 驱动
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        // 如果是新数据库，则创建表结构
        // 注意：Schema 类是 SQLDelight 自动生成的
        try {
            if (!dbFile.exists() || dbFile.length() == 0L) {
                createSchema(driver)
            } else {
                migrateSchema(driver)
            }
        }catch (err: Throwable) {
            println("AppDatabase ${err.message}")
        }
        AppDatabase(driver)
    }

    private fun createSchema(driver: SqlDriver) {
        driver.execute(
            null,
            """
               CREATE TABLE printed_order (
                    platform_id TEXT NOT NULL,
                    order_id TEXT NOT NULL,
                    day_seq TEXT NOT NULL,
                    date TEXT NOT NULL,
                    shop_id TEXT DEFAULT '' ,
                    PRIMARY KEY(platform_id, order_id, shop_id)
                );
            """.trimIndent(),
            0
        )
        version1to2(driver)
        setUserVersion(driver, DB_VERSION)
    }

    private fun migrateSchema(driver: SqlDriver) {
        val currentVersion = driver.execute(
            identifier = null,
            sql = "PRAGMA user_version;",
            parameters = 0
        ).value

        if (currentVersion < DB_VERSION) {
            version1to2(driver)
        }
        setUserVersion(driver, DB_VERSION)
    }

    private fun version1to2(driver: SqlDriver) {
        // printed_order 索引（和 createSchema 保持一致）
        driver.execute(
            null,
            """
                CREATE INDEX IF NOT EXISTS idx_printed_order_shop_date
                ON printed_order(date, shop_id);
            """.trimIndent(),
            0
        )
        // cancel_order 表
        driver.execute(
            null,
            """
            CREATE TABLE IF NOT EXISTS cancel_order (
                platform_id TEXT NOT NULL,
                day_seq TEXT NOT NULL,
                date TEXT NOT NULL,
                order_id TEXT NOT NULL,
                shop_id TEXT DEFAULT '',
                PRIMARY KEY(shop_id, date, order_id)
            );
            """.trimIndent(),
            0
        )
        // cancel_order 索引
        driver.execute(
            null,
            """
            CREATE INDEX IF NOT EXISTS idx_cancel_order_index
            ON cancel_order(date, shop_id);
            """.trimIndent(),
            0
        )
    }

}