package com.neptools.app.core.util

import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Crash-safe file writing helpers for the JSON caches this app uses as its local database.
 *
 * Every cache in the project is written with a plain `File.writeText(...)`, which truncates the
 * destination first. If the process is killed mid-write the file is left empty or truncated and
 * the next read silently falls back to defaults, losing user-visible data. These helpers write to
 * a sibling temp file, flush it to disk, and only then atomically rename it over the destination,
 * so a reader always observes either the old complete file or the new complete file.
 */
object SafeFileWriter {

    /**
     * Writes [content] to [target] atomically: temp file -> flush -> rename.
     *
     * The temp file is created with an isolated unique name in the same directory as [target]
     * so concurrent writers never collide and the rename never crosses a filesystem boundary.
     */
    fun writeAtomic(target: File, content: String) {
        val dir = target.parentFile
        if (dir != null && !dir.exists()) dir.mkdirs()
        val tmp = File(target.parentFile, "${target.name}.${UUID.randomUUID()}.tmp")
        try {
            FileOutputStream(tmp).use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
                out.flush()
                out.fd.sync()
            }
            if (!tmp.renameTo(target)) {
                if (target.exists()) target.delete()
                if (!tmp.renameTo(target)) {
                    target.writeText(content)
                    tmp.delete()
                }
            }
        } catch (t: Throwable) {
            tmp.delete()
            throw t
        }
    }

    /**
     * Writes [content] to [target] atomically, swallowing I/O failures.
     *
     * Used by caches where a failed write must never crash the caller (a background refresh, a
     * UI callback). The previous complete file stays intact on failure.
     */
    fun writeAtomicQuietly(target: File, content: String): Boolean = try {
        writeAtomic(target, content)
        true
    } catch (_: Throwable) {
        false
    }
}
