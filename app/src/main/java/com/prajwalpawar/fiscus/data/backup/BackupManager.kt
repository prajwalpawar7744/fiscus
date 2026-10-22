package com.prajwalpawar.fiscus.data.backup

import android.content.Context
import android.net.Uri
import com.prajwalpawar.fiscus.data.local.FiscusDatabaseProvider
import com.prajwalpawar.fiscus.data.local.ProfilePhotoStore
import com.prajwalpawar.fiscus.data.local.SettingsDataStore
import com.prajwalpawar.fiscus.data.model.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

// What a backup can hold; the user picks any mix when backing up and restoring.
enum class BackupPart {
    // Transactions, items, accounts and categories (the whole database).
    MONEY,

    // Appearance settings.
    SETTINGS,

    // Profile name and photo.
    PROFILE
}

// What a backup file contains, read before restoring.
data class BackupInfo(
    val parts: Set<BackupPart>,
    val createdAt: Long,
    // Database schema version of the backup; null when it has no database.
    val databaseVersion: Int?
)

class BackupException(message: String) : Exception(message)

// A .fiscus file is a zip: manifest.json, plus fiscus.db and profile.jpg when included.
// Settings and the profile name live in the manifest. A backup from an older app version
// restores fine (Room migrates the database on open); a newer one is refused.
class BackupManager(
    context: Context
) {
    private val context = context.applicationContext
    private val settingsDataStore = SettingsDataStore(this.context)
    private val photoStore = ProfilePhotoStore(this.context)

    private val databaseFile: File
        get() = context.getDatabasePath(DATABASE_NAME)

    suspend fun backup(uri: Uri, parts: Set<BackupPart>) = withContext(Dispatchers.IO) {
        require(parts.isNotEmpty())
        val settings = settingsDataStore.settings.first()
        val database = FiscusDatabaseProvider.getDatabase(context)

        val manifest = JSONObject()
            .put("app", "fiscus")
            .put("format", FORMAT_VERSION)
            .put("createdAt", System.currentTimeMillis())
            .put("parts", parts.joinToString(",") { it.name })

        if (BackupPart.MONEY in parts) {
            // Fold the write-ahead log into the main file so the copy is complete.
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
            manifest.put("databaseVersion", database.openHelper.readableDatabase.version)
        }
        if (BackupPart.SETTINGS in parts) {
            manifest.put("settings", settings.toJson())
        }
        if (BackupPart.PROFILE in parts) {
            manifest.put("profileName", settings.profileName)
        }

        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw BackupException("Couldn't open the file to save to.")
        ZipOutputStream(output.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST))
            zip.write(manifest.toString().toByteArray())
            zip.closeEntry()

            if (BackupPart.MONEY in parts) {
                zip.putNextEntry(ZipEntry(DATABASE_ENTRY))
                databaseFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }

            val photo = photoStore.file
            if (BackupPart.PROFILE in parts && settings.profilePhotoVersion != 0L && photo.exists()) {
                zip.putNextEntry(ZipEntry(PHOTO_ENTRY))
                photo.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    suspend fun readInfo(uri: Uri): BackupInfo = withContext(Dispatchers.IO) {
        val manifest = readManifest(uri)
        val parts = manifest.optString("parts").split(",")
            .mapNotNull { name -> BackupPart.entries.firstOrNull { it.name == name } }
            .toSet()
        val databaseVersion = manifest.optInt("databaseVersion", -1).takeIf { it > 0 }

        if (databaseVersion != null && databaseVersion > currentDatabaseVersion()) {
            throw BackupException("This backup is from a newer version of Fiscus. Update the app to restore it.")
        }

        BackupInfo(parts, manifest.optLong("createdAt"), databaseVersion)
    }

    // Replaces the chosen parts with the backup's. Restart the app afterwards.
    suspend fun restore(uri: Uri, parts: Set<BackupPart>) = withContext(Dispatchers.IO) {
        val manifest = readManifest(uri)
        var databaseBytes: ByteArray? = null
        var photoBytes: ByteArray? = null

        // Read everything first, so a broken file changes nothing.
        openZip(uri) { entry, zip ->
            when (entry.name) {
                DATABASE_ENTRY -> if (BackupPart.MONEY in parts) databaseBytes = zip.readBytes()
                PHOTO_ENTRY -> if (BackupPart.PROFILE in parts) photoBytes = zip.readBytes()
            }
        }

        if (BackupPart.MONEY in parts) {
            val bytes = databaseBytes ?: throw BackupException("This backup has no transactions to restore.")
            FiscusDatabaseProvider.close()
            deleteDatabaseFiles()
            databaseFile.writeBytes(bytes)
        }

        if (BackupPart.SETTINGS in parts) {
            manifest.optJSONObject("settings")?.let { settingsDataStore.replaceAppearance(it.toSettings()) }
        }

        if (BackupPart.PROFILE in parts) {
            settingsDataStore.setProfileName(manifest.optString("profileName"))
            val photo = photoBytes
            if (photo != null) {
                photoStore.saveBytes(photo)
                settingsDataStore.setProfilePhotoVersion(System.currentTimeMillis())
            } else {
                photoStore.delete()
                settingsDataStore.setProfilePhotoVersion(0L)
            }
        }
    }

    // Like a fresh install: the database (re-seeded on next open), settings and profile.
    // Restart the app afterwards.
    suspend fun wipe() = withContext(Dispatchers.IO) {
        FiscusDatabaseProvider.close()
        deleteDatabaseFiles()
        settingsDataStore.clear()
        photoStore.delete()
    }

    private fun deleteDatabaseFiles() {
        listOf("", "-wal", "-shm", "-journal").forEach { suffix ->
            File(databaseFile.path + suffix).delete()
        }
    }

    private fun currentDatabaseVersion(): Int =
        FiscusDatabaseProvider.getDatabase(context).openHelper.readableDatabase.version

    private fun readManifest(uri: Uri): JSONObject {
        var manifest: JSONObject? = null
        openZip(uri) { entry, zip ->
            if (entry.name == MANIFEST) manifest = JSONObject(zip.readBytes().decodeToString())
        }
        return manifest?.takeIf { it.optString("app") == "fiscus" }
            ?: throw BackupException("This isn't a Fiscus backup.")
    }

    private fun openZip(uri: Uri, onEntry: (ZipEntry, ZipInputStream) -> Unit) {
        val input = runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()
            ?: throw BackupException("Couldn't open the backup file.")
        try {
            ZipInputStream(input.buffered()).use { zip ->
                generateSequence { zip.nextEntry }.forEach { entry -> onEntry(entry, zip) }
            }
        } catch (e: BackupException) {
            throw e
        } catch (e: Exception) {
            throw BackupException("This isn't a Fiscus backup.")
        }
    }

    private fun AppSettings.toJson(): JSONObject = JSONObject()
        .put("theme", theme.name)
        .put("dynamicColor", dynamicColor)
        .put("palette", palette.name)
        .put("iconShape", iconShape.name)
        .put("appBar", appBar.name)
        .put("bottomBar", bottomBar.name)
        .put("appBarScroll", appBarScroll.name)

    // Unknown values (e.g. from a newer app) fall back to the defaults.
    private fun JSONObject.toSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            theme = enumOr(optString("theme"), defaults.theme),
            dynamicColor = optBoolean("dynamicColor", defaults.dynamicColor),
            palette = enumOr(optString("palette"), defaults.palette),
            iconShape = enumOr(optString("iconShape"), defaults.iconShape),
            appBar = enumOr(optString("appBar"), defaults.appBar),
            bottomBar = enumOr(optString("bottomBar"), defaults.bottomBar),
            appBarScroll = enumOr(optString("appBarScroll"), defaults.appBarScroll)
        )
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    private companion object {
        const val FORMAT_VERSION = 1
        const val DATABASE_NAME = "fiscus.db"
        const val MANIFEST = "manifest.json"
        const val DATABASE_ENTRY = "fiscus.db"
        const val PHOTO_ENTRY = "profile.jpg"
    }
}
