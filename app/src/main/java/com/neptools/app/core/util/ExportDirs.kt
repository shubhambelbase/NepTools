package com.neptools.app.core.util

import android.content.Context
import java.io.File

/**
 * Directories that are explicitly exposed through FileProvider (see res/xml/file_paths.xml).
 *
 * Anything shared with another app via FileProvider MUST live under one of these roots,
 * otherwise FileProvider.getUriForFile throws IllegalArgumentException:
 * "Failed to find configured root that contains <path>".
 */
object ExportDirs {

    /** cache/exports/ - transient, OS-reclaimable sharing output. */
    fun cacheExports(context: Context): File =
        File(context.cacheDir, "exports").apply { mkdirs() }

    /** files/exports/ - keeps sharing output across low-storage cache eviction. */
    fun fileExports(context: Context): File =
        File(context.filesDir, "exports").apply { mkdirs() }
}
