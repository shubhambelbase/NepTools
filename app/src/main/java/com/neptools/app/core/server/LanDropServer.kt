package com.neptools.app.core.server

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import com.neptools.app.core.util.ExportDirs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat

data class SharedFileInfo(
    val id: String,
    val name: String,
    val size: Long,
    val uri: Uri? = null,
    val localFile: File? = null
)

data class LanDropState(
    val isRunning: Boolean = false,
    val ipAddress: String = "",
    val port: Int = 8080,
    val sharedFiles: List<SharedFileInfo> = emptyList(),
    val receivedFiles: List<SharedFileInfo> = emptyList(),
    val sharedText: String = "",
    val lastDownloadedFileName: String = "",
    /** One-time code the browser must supply before it can list or move any file. */
    val sessionCode: String = ""
)

object LanDropServer {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeClients = java.util.concurrent.atomic.AtomicInteger(0)
    private const val MAX_CLIENTS = 12

    private val _state = MutableStateFlow(LanDropState())
    val state = _state.asStateFlow()

    private var appContext: Context? = null
    private const val BUFFER_SIZE = 131072 // 128 KB for high-speed Wi-Fi throughput

    /** The server shuts itself down after this long with no request, so it cannot be left open. */
    private const val IDLE_TIMEOUT_MS = 10L * 60L * 1000L

    /** Consecutive rejected requests tolerated before the server shuts down. */
    private const val MAX_AUTH_FAILURES = 10

    private val authFailures = java.util.concurrent.atomic.AtomicInteger(0)

    @Volatile
    private var lastActivityMs: Long = 0L

    /**
     * When the current transfer session started. The Received list is scoped to files that
     * arrived after this instant, so files from earlier sessions (or a previous phone) do not
     * linger in the app or on the web page. 0 means no session is active.
     */
    @Volatile
    private var sessionStartMs: Long = 0L

    /**
     * Generates the per-session access code. It is displayed on the phone and must be typed into
     * the browser before any file can be listed, downloaded or uploaded.
     */
    private fun generateSessionCode(): String = (100000..999999).random().toString()

    fun getStorageDir(): File {
        // FIX: Use scoped-storage compatible path first, fallback to legacy public dir
        // Environment.getExternalStoragePublicDirectory is deprecated (API 29)
        // Try app-specific external dir which is always writable without permission
        try {
            appContext?.let { ctx ->
                val appDir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "NepTools")
                if (appDir.exists() || appDir.mkdirs()) {
                    // Prefer public Download if we can write there
                    val publicDir = if (android.os.Build.VERSION.SDK_INT >= 29) {
                        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NepTools")
                    } else {
                        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NepTools")
                    }
                    if (publicDir.exists() && publicDir.canWrite()) return publicDir
                    if (!publicDir.exists() && publicDir.mkdirs()) return publicDir
                    // Fallback to app-specific which is guaranteed writable
                    return appDir
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        val candidates = listOf(
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NepTools"),
            File("/storage/emulated/0/Download/NepTools"),
            File("/sdcard/Download/NepTools")
        )
        for (cand in candidates) {
            if (cand.exists() && cand.isDirectory && cand.canWrite()) return cand
        }
        val defaultDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NepTools")
        if (!defaultDir.exists()) defaultDir.mkdirs()
        return defaultDir
    }

    private fun sanitizeFileName(raw: String): String {
        var name = File(raw).name.trim()
        if (name.isBlank()) name = "download_${System.currentTimeMillis()}.bin"
        // Remove path traversal, null bytes, reserved chars
        name = name.replace("\u0000", "").replace("/", "_").replace("\\", "_")
        // Replace Windows reserved chars
        name = name.replace(Regex("[<>:\"|?*]"), "_")
        // Limit length to 120 chars (filesystem limit 255)
        if (name.length > 120) {
            val dot = name.lastIndexOf('.')
            val ext = if (dot > 0) name.substring(dot) else ""
            name = name.take(120 - ext.length) + ext
        }
        // Prevent hidden files and reserved names
        if (name.startsWith(".")) name = "_$name"
        val reserved = setOf("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "LPT1")
        if (reserved.contains(name.substringBefore('.').uppercase())) name = "_$name"
        return name
    }

    private fun uniqueFile(dir: File, name: String): File {
        var file = File(dir, name)
        if (!file.exists()) return file
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var idx = 1
        while (file.exists() && idx < 1000) {
            file = File(dir, "${base}_$idx$ext")
            idx++
        }
        return file
    }

    fun getLocalIpAddress(): String {
        try {
            // FIX: Prefer Wi-Fi interface (wlan0) over mobile/VPN
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: emptyList()
            // First pass: wlan0 / wifi
            for (iface in interfaces) {
                if (iface.isLoopback || !iface.isUp) continue
                val name = iface.name.lowercase()
                val isWifi = name.contains("wlan") || name.contains("ap") || name.contains("wifi")
                if (!isWifi) continue
                for (addr in iface.inetAddresses) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress && addr.hostAddress?.startsWith("127.") != true) {
                        val host = addr.hostAddress ?: continue
                        // Filter link-local 169.254.x.x
                        if (host.startsWith("169.254.")) continue
                        return host
                    }
                }
            }
            // Second pass: any non-loopback IPv4
            for (iface in interfaces) {
                if (iface.isLoopback || !iface.isUp) continue
                for (addr in iface.inetAddresses) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val host = addr.hostAddress ?: continue
                        if (host.startsWith("169.254.")) continue
                        return host
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun startServer(context: Context, port: Int = 8080) {
        appContext = context.applicationContext
        if (_state.value.isRunning) return

        serverJob?.cancel()
        serverJob = scope.launch {
            var currentPort = port
            var tries = 0
            var ss: ServerSocket? = null
            var ip = getLocalIpAddress()
            // FIX: Try up to 5 ports (8080, 8888, 8081, 8082, 8083) instead of single fallback
            while (tries < 5) {
                try {
                    ss = ServerSocket(currentPort)
                    break
                } catch (e: java.net.BindException) {
                    e.printStackTrace()
                    tries++
                    currentPort = when (currentPort) {
                        8080 -> 8888
                        8888 -> 8081
                        else -> currentPort + 1
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
            if (ss == null) {
                _state.value = _state.value.copy(isRunning = false, ipAddress = "", port = port)
                return@launch
            }
            serverSocket = ss
            // Start a fresh session: only uploads that arrive from now on are listed as received.
            sessionStartMs = System.currentTimeMillis()
            refreshReceivedFiles()
            // Re-resolve IP after socket bound (Wi-Fi may have come up)
            ip = getLocalIpAddress().ifEmpty { ip }
            val code = generateSessionCode()
            authFailures.set(0)
            lastActivityMs = System.currentTimeMillis()
            _state.value = _state.value.copy(
                isRunning = true,
                ipAddress = ip,
                port = currentPort,
                sessionCode = code
            )

            // Watchdog: never leave a file-transfer port open indefinitely.
            launch {
                while (_state.value.isRunning) {
                    delay(30_000L)
                    val idle = System.currentTimeMillis() - lastActivityMs
                    if (idle > IDLE_TIMEOUT_MS && activeClients.get() == 0) {
                        stopServer()
                        break
                    }
                }
            }

            while (_state.value.isRunning && !ss.isClosed) {
                try {
                    val client = ss.accept()
                    if (activeClients.get() >= MAX_CLIENTS) {
                        try { client.close() } catch (_: Exception) {}
                        continue
                    }
                    activeClients.incrementAndGet()
                    launch {
                        try { handleClient(client) } finally { activeClients.decrementAndGet() }
                    }
                } catch (e: Exception) {
                    if (_state.value.isRunning) e.printStackTrace()
                    break
                }
            }
        }
    }

    fun stopServer() {
        try {
            _state.value = _state.value.copy(isRunning = false, sessionCode = "")
            serverSocket?.close()
            serverSocket = null
            serverJob?.cancel()
            serverJob = null
            authFailures.set(0)
            // End of session: drop the received list so stale transfers do not linger.
            sessionStartMs = 0L
            refreshReceivedFiles()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addSharedFile(context: Context, uri: Uri) {
        try {
            var name = "file_${System.currentTimeMillis()}"
            var size = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx != -1) name = cursor.getString(nameIdx) ?: name
                    if (sizeIdx != -1) {
                        val v = cursor.getLong(sizeIdx)
                        if (v >= 0) size = v
                    }
                }
            }

            // FIX: available() does NOT return file size; use AssetFileDescriptor or fallback
            if (size <= 0L) {
                try {
                    context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                        if (afd.length >= 0) size = afd.length
                    }
                } catch (_: Exception) {}
            }
            if (size <= 0L) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { ins ->
                        // Do not use available(); mark as unknown (-1) and let download stream without Content-Length if needed
                        // Try to read size via stream length if possible
                        size = -1L
                    }
                } catch (_: Exception) {}
            }

            name = sanitizeFileName(name)
            val id = "${System.currentTimeMillis()}_${Math.abs(name.hashCode())}_${Math.abs(uri.hashCode())}"

            val item = SharedFileInfo(
                id = id,
                name = name,
                size = if (size >= 0) size else -1L,
                uri = uri
            )

            _state.value = _state.value.copy(
                sharedFiles = _state.value.sharedFiles + item
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeSharedFile(id: String) {
        _state.value = _state.value.copy(
            sharedFiles = _state.value.sharedFiles.filter { it.id != id }
        )
    }

    fun setSharedText(text: String) {
        _state.value = _state.value.copy(sharedText = text)
    }

    fun refreshReceivedFiles() {
        try {
            // Session scoping: with no active session the list is empty, and while a session is
            // running only files that arrived during it are shown. Older transfers stay on disk
            // in Download/NepTools/ and remain reachable through the file manager.
            val start = sessionStartMs
            if (start <= 0L) {
                _state.value = _state.value.copy(receivedFiles = emptyList())
                return
            }
            val dir = getStorageDir()
            val files = mutableListOf<File>()
            if (dir.exists()) {
                dir.listFiles()?.filter { it.isFile && it.length() > 0 && it.lastModified() >= start }?.let { files.addAll(it) }
            }
            val alt = File("/sdcard/Download/NepTools")
            if (alt.exists() && alt.canonicalPath != dir.canonicalPath) {
                alt.listFiles()?.filter { it.isFile && it.length() > 0 && it.lastModified() >= start }?.let { altList ->
                    for (af in altList) {
                        if (files.none { it.name == af.name }) files.add(af)
                    }
                }
            }
            val list = files.sortedByDescending { it.lastModified() }.map { f ->
                SharedFileInfo(
                    id = f.name,
                    name = f.name,
                    size = f.length(),
                    localFile = f
                )
            }
            _state.value = _state.value.copy(receivedFiles = list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.soTimeout = 45000
            socket.receiveBufferSize = BUFFER_SIZE
            socket.sendBufferSize = BUFFER_SIZE

            val input = BufferedInputStream(socket.getInputStream(), BUFFER_SIZE)
            val output = BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE)

            // Read HTTP Request line & headers
            val headerBytes = ByteArrayOutputStream()
            var prev = 0
            var matched = 0

            // Read until CRLF CRLF
            while (true) {
                val b = input.read()
                if (b == -1) break
                headerBytes.write(b)
                if (prev == '\r'.code && b == '\n'.code) {
                    matched++
                    if (matched == 2) break
                } else if (b != '\r'.code) {
                    matched = 0
                }
                prev = b
            }

            val headerStr = headerBytes.toString("UTF-8")
            val lines = headerStr.split("\r\n")
            if (lines.isEmpty()) return

            val reqLine = lines[0].split(" ")
            if (reqLine.size < 2) return
            val method = reqLine[0].uppercase()
            val rawPath = reqLine[1].substringBefore("?").substringBefore("#")
            val rawQuery = reqLine[1].substringAfter("?", "").substringBefore("#")
            val path = URLDecoder.decode(rawPath, "UTF-8")
            val queryParams = parseQuery(rawQuery)

            var contentLength = 0L
            var boundary = ""
            var isChunked = false
            var headerToken = ""

            for (h in lines) {
                if (h.lowercase().startsWith("x-neptools-token:")) {
                    headerToken = h.substringAfter(":").trim()
                }
                val lower = h.lowercase()
                if (lower.startsWith("content-length:")) {
                    contentLength = h.substringAfter(":").trim().toLongOrNull() ?: 0L
                }
                if (lower.startsWith("content-type:") && lower.contains("boundary=")) {
                    // FIX: Extract boundary correctly with optional quotes and params after
                    val after = h.substringAfter("boundary=", "")
                    boundary = after.split(";")[0].trim().trim('"', '\'').trim()
                }
                if (lower.startsWith("transfer-encoding:") && lower.contains("chunked")) {
                    isChunked = true
                }
            }

            // A request is authorised when it presents the current session code either as the
            // "k" query parameter (used by browser links) or as the X-Neptools-Token header.
            val suppliedCode = queryParams["k"] ?: headerToken
            val authorized = suppliedCode.isNotEmpty() &&
                suppliedCode == _state.value.sessionCode &&
                _state.value.isRunning
            // Only authorised traffic counts as activity for the idle watchdog; unauthenticated
            // probes must not be able to keep the port open.
            if (authorized) lastActivityMs = System.currentTimeMillis()

            when {
                path == "/unlock" -> {
                    handleUnlock(queryParams["code"].orEmpty(), output)
                }
                path == "/" || path == "/index.html" || path == "/app.html" -> {
                    if (authorized) serveWebPage(output) else servePinGate(output, wrongCode = false)
                }
                !authorized -> {
                    rejectUnauthorized(output)
                }
                path == "/api/state" -> {
                    serveApiState(output)
                }
                path == "/api/files" -> {
                    serveJsonFiles(output)
                }
                path == "/downloadAll" || path == "/downloadAll.zip" -> {
                    serveDownloadAll(output)
                }
                path.startsWith("/download/") -> {
                    val fileId = URLDecoder.decode(path.removePrefix("/download/"), "UTF-8")
                    serveDownload(fileId, output)
                }
                path == "/upload" && method == "POST" -> {
                    if (boundary.isEmpty()) {
                        send404(output)
                    } else if (isChunked) {
                        // Chunked upload not supported – inform client
                        sendError(output, 411, "Length Required - chunked not supported")
                    } else {
                        handleMultipartBinaryUpload(input, boundary, contentLength, output)
                    }
                }
                path == "/text" && method == "POST" -> {
                    // FIX: Respect Content-Length fully, limit to 64KB but read exactly
                    val len = contentLength.coerceAtMost(65536L).toInt().coerceAtLeast(0)
                    val bodyBuf = ByteArray(len)
                    var readTotal = 0
                    while (readTotal < len) {
                        val r = input.read(bodyBuf, readTotal, len - readTotal)
                        if (r == -1) break
                        readTotal += r
                    }
                    val body = String(bodyBuf, 0, readTotal, StandardCharsets.UTF_8)
                    val text = URLDecoder.decode(body.substringAfter("text=", ""), "UTF-8").take(10000)
                    _state.value = _state.value.copy(sharedText = text)
                    // JSON response so the single-page client does not need a full reload.
                    val rb = "{\"ok\":true}".toByteArray(StandardCharsets.UTF_8)
                    output.write(
                        ("HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${rb.size}\r\nCache-Control: no-store\r\nConnection: close\r\n\r\n")
                            .toByteArray(StandardCharsets.UTF_8)
                    )
                    output.write(rb)
                }
                else -> {
                    send404(output)
                }
            }

            output.flush()
            socket.close()
        } catch (e: Exception) {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun handleMultipartBinaryUpload(
        input: InputStream,
        boundary: String,
        contentLength: Long,
        out: OutputStream
    ) {
        try {
            if (boundary.isBlank() || contentLength <= 0) {
                sendError(out, 400, "Invalid upload request")
                return
            }
            val maxUpload = 500L * 1024 * 1024
            if (contentLength > maxUpload) {
                sendError(out, 413, "File too large (max 500 MB total)")
                return
            }
            val delimiter = ("\r\n--" + boundary).toByteArray(StandardCharsets.ISO_8859_1)
            val endDelimiter = ("--" + boundary + "--").toByteArray(StandardCharsets.ISO_8859_1)
            val headerEnd = "\r\n\r\n".toByteArray(StandardCharsets.ISO_8859_1)
            val targetDir = getStorageDir()
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                sendError(out, 500, "Cannot create download dir")
                return
            }
            try {
                val free = targetDir.freeSpace
                if (free in 1 until contentLength) {
                    sendError(out, 507, "Insufficient storage")
                    return
                }
            } catch (_: Exception) {}

            // FIX: Support multiple files in one multipart request (HTML multiple)
            // We parse parts sequentially using a buffered input that preserves leftover bytes
            val savedNames = mutableListOf<String>()
            var pending = ByteArrayOutputStream() // holds leftover bytes after delimiter
            var partIndex = 0
            val maxFiles = 20

            // Helper to read next part headers (handles leftover from previous part)
            fun readNextPartHeaders(): String? {
                val buf = ByteArrayOutputStream()
                // First drain pending if it contains headerEnd
                if (pending.size() > 0) {
                    val pendBytes = pending.toByteArray()
                    val idx = indexOfSubarray(pendBytes, headerEnd)
                    if (idx != -1) {
                        buf.write(pendBytes, 0, idx + headerEnd.size)
                        pending.reset()
                        if (pendBytes.size > idx + headerEnd.size) {
                            pending.write(pendBytes, idx + headerEnd.size, pendBytes.size - idx - headerEnd.size)
                        }
                        return buf.toString("UTF-8")
                    } else {
                        buf.write(pendBytes)
                        pending.reset()
                    }
                }
                var b: Int
                while (input.read().also { b = it } != -1) {
                    buf.write(b)
                    val bytes = buf.toByteArray()
                    if (bytes.size >= 4 && bytes.takeLast(4).toByteArray().contentEquals(headerEnd)) {
                        return buf.toString("UTF-8")
                    }
                    if (bytes.size > 16384) return null // header bomb
                    // Also stop if we hit end delimiter directly (no more parts)
                    if (bytes.size >= endDelimiter.size && indexOfSubarray(bytes, endDelimiter) != -1) return null
                }
                return if (buf.size() > 0) buf.toString("UTF-8") else null
            }

            while (partIndex < maxFiles) {
                val partHeaders = readNextPartHeaders() ?: break
                // Check if this is the final boundary without file
                if (partHeaders.contains(boundary + "--") && !partHeaders.contains("filename=")) {
                    // Could be end marker, check pending for more
                    if (pending.size() == 0) break
                }
                var filename: String? = null
                var isFilePart = false
                if (partHeaders.contains("filename=")) {
                    isFilePart = true
                    val rawCandidate = partHeaders.substringAfter("filename=", "").trim()
                    val raw = when {
                        rawCandidate.startsWith("\"") -> rawCandidate.substringAfter("\"").substringBefore("\"")
                        rawCandidate.startsWith("'") -> rawCandidate.substringAfter("'").substringBefore("'")
                        else -> rawCandidate.split(";")[0].split("\r")[0].split("\n")[0].trim().trim('"', '\'')
                    }
                    filename = if (raw.isNotBlank()) sanitizeFileName(raw) else "download_${System.currentTimeMillis()}_$partIndex.bin"
                } else {
                    // Non-file form field (e.g., text), skip its body until next delimiter
                    filename = null
                }

                if (!isFilePart) {
                    // Skip non-file part body until delimiter
                    val skipBuf = ByteArrayOutputStream()
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    var found = false
                    // Seed skipBuf with pending
                    if (pending.size() > 0) {
                        skipBuf.write(pending.toByteArray())
                        pending.reset()
                    }
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        skipBuf.write(buffer, 0, bytesRead)
                        val all = skipBuf.toByteArray()
                        if (indexOfSubarray(all, delimiter) != -1 || indexOfSubarray(all, endDelimiter) != -1) {
                            // Preserve bytes after delimiter for next header
                            val idx = indexOfSubarray(all, delimiter).let { if (it != -1) it else indexOfSubarray(all, endDelimiter) }
                            if (idx != -1 && all.size > idx + delimiter.size) {
                                pending.write(all, idx + delimiter.size, all.size - idx - delimiter.size)
                            } else if (idx != -1 && idx >= 0) {
                                // keep remainder for next header
                            }
                            found = true
                            break
                        }
                        // Keep overlap
                        if (all.size > delimiter.size) {
                            val keep = delimiter.size
                            val trimmed = all.copyOfRange(0, all.size - keep)
                            // discard trimmed (non-file field content not needed)
                            skipBuf.reset()
                            skipBuf.write(all, all.size - keep, keep)
                        }
                        if (skipBuf.size() > 65536) break // avoid infinite skip for huge text field
                    }
                    if (!found) break
                    continue
                }

                // File part -> stream to disk
                val safeName = sanitizeFileName(filename!!)
                val targetFile = uniqueFile(targetDir, safeName)
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(targetFile)
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    val streamBuf = ByteArrayOutputStream()
                    // Seed with pending leftover (start of file content if header read consumed extra)
                    if (pending.size() > 0) {
                        streamBuf.write(pending.toByteArray())
                        pending.reset()
                    }
                    var totalWritten = 0L
                    var foundBoundary = false
                    var pendingTail: ByteArray? = null
                    while (true) {
                        // Check if streamBuf already contains delimiter
                        val allBytesCheck = streamBuf.toByteArray()
                        val idxCheck = indexOfSubarray(allBytesCheck, delimiter)
                        val endIdxCheck = indexOfSubarray(allBytesCheck, endDelimiter)
                        val idxFound = if (idxCheck != -1) idxCheck else if (endIdxCheck != -1) endIdxCheck else -1
                        if (idxFound != -1) {
                            if (idxFound > 0) fos.write(allBytesCheck, 0, idxFound)
                            // Save leftover after delimiter for next part headers
                            val after = idxFound + delimiter.size
                            if (allBytesCheck.size > after) {
                                pending.write(allBytesCheck, after, allBytesCheck.size - after)
                            } else if (endIdxCheck != -1) {
                                // Final boundary, no more parts
                                pending.reset()
                            }
                            foundBoundary = true
                            break
                        }
                        // Need more data
                        val r = input.read(buffer)
                        if (r == -1) {
                            // No more data, flush what we have (no delimiter found -> incomplete)
                            if (streamBuf.size() > 0) {
                                fos.write(streamBuf.toByteArray())
                            }
                            break
                        }
                        bytesRead = r
                        streamBuf.write(buffer, 0, bytesRead)
                        val allBytes = streamBuf.toByteArray()
                        val bIdx = indexOfSubarray(allBytes, delimiter)
                        val eIdx = indexOfSubarray(allBytes, endDelimiter)
                        val fIdx = if (bIdx != -1) bIdx else if (eIdx != -1) eIdx else -1
                        if (fIdx != -1) {
                            if (fIdx > 0) fos.write(allBytes, 0, fIdx)
                            val after = fIdx + delimiter.size
                            if (allBytes.size > after) {
                                pending.write(allBytes, after, allBytes.size - after)
                            }
                            foundBoundary = true
                            break
                        } else {
                            val safeWriteLen = allBytes.size - delimiter.size + 1
                            if (safeWriteLen > 0) {
                                fos.write(allBytes, 0, safeWriteLen)
                                val remaining = allBytes.copyOfRange(safeWriteLen, allBytes.size)
                                streamBuf.reset()
                                streamBuf.write(remaining)
                            }
                        }
                        if (streamBuf.size() > maxUpload) break
                    }
                    fos.flush()
                    fos.close()
                    fos = null
                    if (targetFile.length() == 0L && !savedNames.contains(targetFile.name)) {
                        // Allow empty but still count if it was intentional; otherwise delete if no boundary found
                        // Keep empty file (user may have uploaded empty)
                    }
                    appContext?.let { ctx -> MediaScannerConnection.scanFile(ctx, arrayOf(targetFile.absolutePath), null, null) }
                    savedNames.add(targetFile.name)
                    partIndex++
                    // If pending contains final boundary marker, stop
                    val pendStr = pending.toString("UTF-8")
                    if (pendStr.contains(boundary + "--")) break
                    // If we consumed final delimiter (endDelimiter without \r\n), break
                    if (foundBoundary && pending.size() == 0) {
                        // Peek if next bytes would be -- (final); if input has no more, break on next header read returning null
                    }
                } finally {
                    try { fos?.close() } catch (_: Exception) {}
                }
            }

            if (savedNames.isEmpty()) {
                sendError(out, 400, "No file uploaded")
                return
            }
            refreshReceivedFiles()
            _state.value = _state.value.copy(lastDownloadedFileName = savedNames.last())
            // JSON body lets the SPA mark the upload complete without a page reload.
            val payload = JSONObject().apply {
                put("ok", true)
                put("saved", JSONArray(savedNames))
            }
            val rb = payload.toString().toByteArray(StandardCharsets.UTF_8)
            out.write(
                ("HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=UTF-8\r\n" +
                    "Content-Length: ${rb.size}\r\nCache-Control: no-store\r\nConnection: close\r\n\r\n")
                    .toByteArray(StandardCharsets.UTF_8)
            )
            out.write(rb)
        } catch (e: Exception) {
            e.printStackTrace()
            sendError(out, 500, "Upload failed: ${e.message}")
        }
    }

    private fun indexOfSubarray(array: ByteArray, sub: ByteArray): Int {
        if (sub.isEmpty() || array.size < sub.size) return -1
        for (i in 0..array.size - sub.size) {
            var found = true
            for (j in sub.indices) {
                if (array[i + j] != sub[j]) {
                    found = false
                    break
                }
            }
            if (found) return i
        }
        return -1
    }

    private fun parseQuery(rawQuery: String): Map<String, String> {
        if (rawQuery.isBlank()) return emptyMap()
        val out = HashMap<String, String>()
        for (pair in rawQuery.split("&")) {
            if (pair.isBlank()) continue
            val key = pair.substringBefore("=", "")
            val value = pair.substringAfter("=", "")
            if (key.isNotBlank()) {
                out[URLDecoder.decode(key, "UTF-8")] = URLDecoder.decode(value, "UTF-8")
            }
        }
        return out
    }

    private fun handleUnlock(suppliedCode: String, out: OutputStream) {
        val expected = _state.value.sessionCode
        if (expected.isNotEmpty() && suppliedCode == expected) {
            authFailures.set(0)
            sendRedirect(out, "/?k=${URLEncoder.encode(expected, "UTF-8")}")
            return
        }
        registerAuthFailure()
        servePinGate(out, wrongCode = true)
    }

    private fun rejectUnauthorized(out: OutputStream) {
        // Deliberately NOT counted toward the brute-force lockout: a browser tab left open
        // across a server restart polls with a stale code and must not kill the session.
        // Guessing is funnelled through /unlock, where failures do count.
        val body = "{\"error\":\"Unauthorized. Enter the session code shown on the phone.\"}"
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        val header = "HTTP/1.1 403 Forbidden\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Cache-Control: no-store\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(bytes)
    }

    /**
     * Counts consecutive rejected requests and shuts the server down once an attacker on the
     * network starts guessing, so the session code cannot be brute forced.
     */
    private fun registerAuthFailure() {
        val failures = authFailures.incrementAndGet()
        if (failures >= MAX_AUTH_FAILURES) stopServer()
    }

    private fun servePinGate(out: OutputStream, wrongCode: Boolean) {
        val message = if (wrongCode) {
            "Incorrect code. Check the code shown in the NepTools app."
        } else {
            "Enter the 6-digit session code shown in the NepTools app on your phone."
        }
        val messageColor = if (wrongCode) "#f87171" else "#94a3b8"
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NepTools LAN Drop - Locked</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: radial-gradient(900px 500px at 80% -10%, rgba(225,29,72,.14), transparent 60%), #0b1220; color: #f1f5f9; margin: 0; min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 16px; }
        .card { background: #141d31; border: 1px solid #273449; border-radius: 18px; padding: 28px; max-width: 380px; width: 100%; text-align: center; box-shadow: 0 8px 24px rgba(0,0,0,.35); }
        .logo { width: 52px; height: 52px; background: linear-gradient(135deg, #e11d48, #be123c); border-radius: 14px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 800; font-size: 20px; margin: 0 auto 16px auto; box-shadow: 0 6px 20px rgba(225,29,72,.35); }
        h1 { font-size: 19px; margin: 0 0 8px 0; }
        p { color: $messageColor; font-size: 13px; margin: 0 0 18px 0; line-height: 1.5; }
        input { width: 100%; box-sizing: border-box; background: #0f172a; color: #fff; border: 1px solid #273449; border-radius: 12px; padding: 14px; font-size: 22px; letter-spacing: 8px; text-align: center; font-weight: 700; }
        input:focus { outline: none; border-color: #e11d48; }
        button { width: 100%; margin-top: 14px; background: #e11d48; color: #fff; border: none; padding: 14px; border-radius: 12px; font-weight: 700; font-size: 15px; cursor: pointer; }
        button:hover { background: #be123c; }
        .note { color: #64748b; font-size: 11px; margin-top: 16px; }
    </style>
</head>
<body>
    <div class="card">
        <div class="logo">NT</div>
        <h1>NepTools LAN Drop</h1>
        <p>$message</p>
        <form action="/unlock" method="get" autocomplete="off">
            <input type="tel" name="code" inputmode="numeric" pattern="[0-9]*" maxlength="6" placeholder="000000" autofocus required>
            <button type="submit">Unlock</button>
        </form>
        <div class="note">Only devices you authorise on this network can transfer files.</div>
    </div>
</body>
</html>
        """
        val bytes = html.toByteArray(StandardCharsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Cache-Control: no-store\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(bytes)
    }

    /**
     * Serves the single-page LAN Drop client from assets. The page polls /api/state so
     * phone-side changes show up live without the user refreshing the browser.
     */
    private fun serveWebPage(out: OutputStream) {
        val ctx = appContext
        val assetBytes: ByteArray? = try {
            ctx?.assets?.open("landrop.html")?.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (assetBytes == null) {
            sendError(out, 500, "LAN Drop page missing from app assets")
            return
        }
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: ${assetBytes.size}\r\n" +
                "Cache-Control: no-store\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(assetBytes)
    }

    /**
     * Full live state for the single-page client. The browser polls this every couple of
     * seconds, which is how files added on the phone appear on the web page without a refresh.
     */
    private fun serveApiState(out: OutputStream) {
        val s = _state.value
        val token = URLEncoder.encode(s.sessionCode, "UTF-8")
        val root = JSONObject()
        root.put("code", s.sessionCode)
        root.put("connected", s.isRunning)
        root.put("text", s.sharedText)
        val shared = JSONArray()
        for (item in s.sharedFiles) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("size", item.size)
            obj.put("downloadUrl", "/download/${URLEncoder.encode(item.id, "UTF-8").replace("+", "%20")}?k=$token")
            shared.put(obj)
        }
        root.put("shared", shared)
        val received = JSONArray()
        val df = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        for (item in s.receivedFiles) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("size", item.size)
            obj.put("time", df.format(java.util.Date(item.localFile?.lastModified() ?: 0L)))
            received.put(obj)
        }
        root.put("received", received)
        val bytes = root.toString().toByteArray(StandardCharsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\nCache-Control: no-store\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n"
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(bytes)
    }

    private fun serveJsonFiles(out: OutputStream) {
        val list = _state.value.sharedFiles
        val token = URLEncoder.encode(_state.value.sessionCode, "UTF-8")
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("size", item.size)
            val encId = URLEncoder.encode(item.id, "UTF-8").replace("+", "%20")
            obj.put("downloadUrl", "/download/$encId?k=$token")
            arr.put(obj)
        }
        val bytes = arr.toString().toByteArray(StandardCharsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=UTF-8\r\nContent-Length: ${bytes.size}\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n"
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(bytes)
    }

    private fun serveDownloadAll(out: OutputStream) {
        val ctx = appContext ?: return send404(out)
        val files = _state.value.sharedFiles
        if (files.isEmpty()) return sendError(out, 404, "No files to download")
        var zipFile: File? = null
        try {
            zipFile = File(ctx.cacheDir, "neptools_shared_${System.currentTimeMillis()}.zip")
            java.util.zip.ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                val seen = HashSet<String>()
                for (item in files) {
                    try {
                        val name = sanitizeFileName(item.name).let { n ->
                            var unique = n
                            var idx = 1
                            while (!seen.add(unique)) {
                                val dot = n.lastIndexOf('.')
                                val base = if (dot > 0) n.substring(0, dot) else n
                                val ext = if (dot > 0) n.substring(dot) else ""
                                unique = "${base}_$idx$ext"
                                idx++
                            }
                            unique
                        }
                        ctx.contentResolver.openInputStream(item.uri ?: continue)?.use { ins ->
                            zos.putNextEntry(java.util.zip.ZipEntry(name))
                            val buf = ByteArray(BUFFER_SIZE)
                            var r: Int
                            while (ins.read(buf).also { r = it } != -1) zos.write(buf, 0, r)
                            zos.closeEntry()
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
            val len = zipFile.length()
            val header = "HTTP/1.1 200 OK\r\nContent-Type: application/zip\r\nContent-Disposition: attachment; filename=\"NepTools_Files.zip\"; filename*=UTF-8''NepTools_Files.zip\r\nContent-Length: $len\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n"
            out.write(header.toByteArray(StandardCharsets.UTF_8))
            FileInputStream(zipFile).use { fis ->
                val buf = ByteArray(BUFFER_SIZE)
                var r: Int
                while (fis.read(buf).also { r = it } != -1) out.write(buf, 0, r)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendError(out, 500, "Zip failed: ${e.message}")
        } finally {
            try { zipFile?.delete() } catch (_: Exception) {}
        }
    }

    fun createReceivedZip(context: Context): File? {
        val files = _state.value.receivedFiles
        if (files.isEmpty()) return null
        return try {
            val zipFile = File(ExportDirs.cacheExports(context), "NepTools_Received_${System.currentTimeMillis()}.zip")
            java.util.zip.ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                val seen = HashSet<String>()
                for (f in files) {
                    val src = f.localFile ?: File(getStorageDir(), f.name)
                    if (!src.exists() || !src.isFile) continue
                    var name = sanitizeFileName(src.name)
                    var unique = name
                    var idx = 1
                    while (!seen.add(unique)) {
                        val dot = name.lastIndexOf('.')
                        val base = if (dot > 0) name.substring(0, dot) else name
                        val ext = if (dot > 0) name.substring(dot) else ""
                        unique = "${base}_$idx$ext"
                        idx++
                    }
                    name = unique
                    FileInputStream(src).use { fis ->
                        zos.putNextEntry(java.util.zip.ZipEntry(name))
                        val buf = ByteArray(BUFFER_SIZE)
                        var r: Int
                        while (fis.read(buf).also { r = it } != -1) zos.write(buf, 0, r)
                        zos.closeEntry()
                    }
                }
            }
            zipFile
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    fun createSharedZip(context: Context): File? {
        val files = _state.value.sharedFiles
        if (files.isEmpty()) return null
        return try {
            val zipFile = File(ExportDirs.cacheExports(context), "NepTools_Shared_${System.currentTimeMillis()}.zip")
            java.util.zip.ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                val seen = HashSet<String>()
                for (item in files) {
                    try {
                        var name = sanitizeFileName(item.name)
                        var unique = name
                        var idx = 1
                        while (!seen.add(unique)) {
                            val dot = name.lastIndexOf('.')
                            val base = if (dot > 0) name.substring(0, dot) else name
                            val ext = if (dot > 0) name.substring(dot) else ""
                            unique = "${base}_$idx$ext"
                            idx++
                        }
                        name = unique
                        context.contentResolver.openInputStream(item.uri ?: continue)?.use { ins ->
                            zos.putNextEntry(java.util.zip.ZipEntry(name))
                            val buf = ByteArray(BUFFER_SIZE)
                            var r: Int
                            while (ins.read(buf).also { r = it } != -1) zos.write(buf, 0, r)
                            zos.closeEntry()
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
            zipFile
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    private fun serveDownload(fileId: String, out: OutputStream) {
        val fileInfo = _state.value.sharedFiles.find { it.id == fileId }
        val ctx = appContext

        if (fileInfo != null && fileInfo.uri != null && ctx != null) {
            var inStream: InputStream? = null
            try {
                inStream = ctx.contentResolver.openInputStream(fileInfo.uri) ?: return send404(out)
                val encodedName = URLEncoder.encode(fileInfo.name, "UTF-8").replace("+", "%20")
                // FIX: Use actual stream size when available, otherwise omit Content-Length (chunked) or use -1 size fallback
                val actualSize = try {
                    ctx.contentResolver.openAssetFileDescriptor(fileInfo.uri, "r")?.use { it.length } ?: -1L
                } catch (_: Exception) { -1L }
                val sizeHeader = if (actualSize >= 0) "Content-Length: $actualSize\r\n" else ""
                // RFC 6266: provide both filename and filename* for UTF-8
                val header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/octet-stream\r\n" +
                        "Content-Disposition: attachment; filename=\"$encodedName\"; filename*=UTF-8''$encodedName\r\n" +
                        sizeHeader +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Connection: close\r\n\r\n"
                out.write(header.toByteArray(StandardCharsets.UTF_8))

                val buf = ByteArray(BUFFER_SIZE)
                var read: Int
                while (inStream.read(buf).also { read = it } != -1) {
                    out.write(buf, 0, read)
                }
                return
            } catch (e: Exception) {
                e.printStackTrace()
                // If header already sent, can't send 404; just close
                try { send404(out) } catch (_: Exception) {}
            } finally {
                try { inStream?.close() } catch (_: Exception) {}
            }
        }
        send404(out)
    }

    private fun sendRedirect(out: OutputStream, location: String) {
        val resp = "HTTP/1.1 303 See Other\r\nLocation: $location\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
        out.write(resp.toByteArray(StandardCharsets.UTF_8))
    }

    private fun send404(out: OutputStream) {
        val msg = "404 Not Found"
        val resp = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: ${msg.length}\r\nConnection: close\r\n\r\n$msg"
        out.write(resp.toByteArray(StandardCharsets.UTF_8))
    }

    private fun sendError(out: OutputStream, code: Int, message: String) {
        val body = "$code $message"
        val status = when (code) {
            400 -> "400 Bad Request"
            404 -> "404 Not Found"
            411 -> "411 Length Required"
            413 -> "413 Payload Too Large"
            500 -> "500 Internal Server Error"
            507 -> "507 Insufficient Storage"
            else -> "$code Error"
        }
        val resp = "HTTP/1.1 $status\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: ${body.toByteArray(StandardCharsets.UTF_8).size}\r\nConnection: close\r\n\r\n$body"
        out.write(resp.toByteArray(StandardCharsets.UTF_8))
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "Unknown"
        val df = DecimalFormat("#.##")
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
            bytes >= 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
            bytes >= 1024 -> "${df.format(bytes / 1024.0)} KB"
            else -> "$bytes B"
        }
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    }
}
