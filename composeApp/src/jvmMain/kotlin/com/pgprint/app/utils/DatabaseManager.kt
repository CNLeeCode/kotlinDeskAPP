package com.pgprint.app.utils
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.pgprint.app.BuildConfig.STORED_DIR
import com.pgprint.app.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object DatabaseManager {
    private const val DB_NAME = "pgprint.db"

    private const val DB_VERSION = 2 // 当前数据库版本

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
                migrateSchema(driver, oldVersion = 1)
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
            CREATE TABLE ConnectionInfo (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dateText TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                connectionDetail TEXT NOT NULL,
                textColor TEXT DEFAULT '#07c160'
            )
            """.trimIndent(),
            0
        )
        driver.execute(
            null,
            """
               CREATE TABLE printed_order (
                    platform_id TEXT NOT NULL,
                    order_id TEXT NOT NULL,
                    day_seq TEXT NOT NULL,
                    date TEXT NOT NULL,
                    PRIMARY KEY(platform_id, order_id)
                );
                
                CREATE INDEX idx_printed_order_time ON printed_order(platform_id, date);
            """.trimIndent(),
            0
        )
    }

    private fun migrateSchema(driver: SqlDriver, oldVersion: Int) {
        if (oldVersion < DB_VERSION) {
            driver.execute(
                null,
                "ALTER TABLE ConnectionInfo ADD COLUMN textColor TEXT DEFAULT '#07c160'",
                0
            )
        }
    }

    fun deleteHistoryByDate(date: String) {
        dbScope.launch {
            database.connectionInfoQueries.deleteByDate(date)
        }
    }

    suspend fun deleteYesterdayHistory() {
        val currentDate: LocalDate = LocalDate.now()
        val yesterdayDate: LocalDate = currentDate.minusDays(1)
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val yesterday: String = yesterdayDate.format(formatter)
        withContext(Dispatchers.IO) {
            deleteHistoryByDate(yesterday)
        }
    }
}