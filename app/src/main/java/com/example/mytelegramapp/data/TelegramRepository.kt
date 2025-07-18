package com.example.mytelegramapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mytelegramapp.data.model.Session
import com.example.mytelegramapp.data.model.VerificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

// Клас ResultHandler виведено за межі об'єкта для уникнення помилок компіляції
private class ResultHandler(private val onUpdate: (TdApi.Object) -> Unit) : Client.ResultHandler {
    override fun onResult(obj: TdApi.Object) {
        onUpdate(obj)
    }
}

object TelegramRepository {

    private const val API_ID = 21983803
    private const val API_HASH = "c8debebe09581765d1c9cbc4d159c3dae9"
    private const val DROPBOX_SESSIONS_URL = "https://www.dropbox.com/scl/fo/72312mrh7mrsvwcwn28t0/AI8jAbd1NqG-Rcf3oFZyRwA?rlkey=qof7usumjz53eg7viz3oepbkh&st=7kcflchy&dl=1"

    private var client: Client? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _logMessages = MutableLiveData<String>()
    val logMessages: LiveData<String> = _logMessages

    private val _authState = MutableLiveData<TdApi.AuthorizationState>()
    val authState: LiveData<TdApi.AuthorizationState> = _authState

    private val _sessions = MutableLiveData<List<Session>>()
    val sessions: LiveData<List<Session>> = _sessions

    private lateinit var appContext: Context
    private var pendingPhoneNumber: String? = null

    fun initialize(context: Context) {
        if (client == null) {
            this.appContext = context.applicationContext
            val handler = ResultHandler { obj ->
                if (obj is TdApi.UpdateAuthorizationState) {
                    handleAuthorizationState(obj.authorizationState)
                }
            }
            client = Client.create(handler, null, null)
            log("Клиент TDLib создан.")
        }
    }

    private fun setTdlibParameters() {
        val parameters = TdApi.TdlibParameters().apply {
            useTestDc = false
            databaseDirectory = File(appContext.filesDir, "tdlib").absolutePath
            filesDirectory = File(appContext.filesDir, "tdlib-files").absolutePath
            useFileDatabase = true
            useChatInfoDatabase = true
            useMessageDatabase = true
            apiId = API_ID
            apiHash = API_HASH
            systemLanguageCode = appContext.getSystemService(Context.LOCALE_SERVICE)?.toString() ?: "en"
            deviceModel = android.os.Build.MODEL
            systemVersion = android.os.Build.VERSION.RELEASE
            applicationVersion = "1.0" // Рекомендується оновити до актуальної версії
        }
        send(TdApi.SetTdlibParameters(parameters))
    }

    private fun handleAuthorizationState(authorizationState: TdApi.AuthorizationState) {
        when (authorizationState) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> setTdlibParameters()
            is TdApi.AuthorizationStateWaitEncryptionKey -> send(TdApi.CheckDatabaseEncryptionKey())
            is TdApi.AuthorizationStateReady -> {
                log("Авторизация успешна. Клиент готов.")
                pendingPhoneNumber?.let {
                    saveSessionFile(it)
                    pendingPhoneNumber = null
                }
            }
        }
        _authState.postValue(authorizationState)
    }

    private fun send(query: TdApi.Function, callback: ((TdApi.Object) -> Unit)? = null) {
        client?.send(query, callback)
    }

    private fun log(message: String) {
        _logMessages.postValue(message)
    }

    fun sendPhoneNumber(phone: String) {
        pendingPhoneNumber = phone
        log("Отправка номера телефона: $phone")
        send(TdApi.SetAuthenticationPhoneNumber(phone, null))
    }

    fun sendCode(code: String) {
        log("Отправка кода подтверждения...")
        send(TdApi.CheckAuthenticationCode(code))
    }

    fun sendPassword(password: String) {
        log("Отправка пароля 2FA...")
        send(TdApi.CheckAuthenticationPassword(password))
    }

    fun loadSessions(context: Context) {
        val sessionDir = File(context.filesDir, "sessions")
        if (!sessionDir.exists()) {
            sessionDir.mkdirs()
        }
        val sessionFiles = sessionDir.listFiles { _, name -> name.endsWith(".session") }
        val sessionList = sessionFiles?.map { Session(it.name) } ?: emptyList()
        _sessions.postValue(sessionList)
    }

    fun downloadSessions(context: Context) {
        scope.launch {
            log("⬇️ Загрузка сессий из облака...")
            val sessionDir = File(context.filesDir, "sessions")
            sessionDir.mkdirs()
            val zipFile = File(context.cacheDir, "sessions.zip")

            try {
                val httpClient = OkHttpClient()
                val request = Request.Builder().url(DROPBOX_SESSIONS_URL).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        log("❌ Ошибка загрузки: ${response.message}")
                        return@use
                    }
                    FileOutputStream(zipFile).use { fileOut ->
                        response.body!!.byteStream().use { bodyIn ->
                            bodyIn.copyTo(fileOut)
                        }
                    }
                }

                log("Архив скачан. Распаковка...")
                ZipInputStream(zipFile.inputStream()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val cleanName = entry.name.split("/").lastOrNull() ?: ""
                        if (cleanName.isNotEmpty() && !entry.isDirectory) {
                            val file = File(sessionDir, cleanName)
                            FileOutputStream(file).use { fos ->
                                zip.copyTo(fos)
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
                log("✅ Сессии успешно загружены и распакованы.")
                loadSessions(context)
            } catch (e: Exception) {
                log("❌ Ошибка распаковки: ${e.message}")
                e.printStackTrace()
            } finally {
                if (zipFile.exists()) {
                    zipFile.delete()
                }
            }
        }
    }

    fun verifyAllSessions(context: Context) {
        val currentSessions = _sessions.value ?: return
        if (currentSessions.isEmpty()) {
            log("Нет сессий для проверки.")
            return
        }
        currentSessions.forEach { it.status = VerificationStatus.VERIFYING }
        _sessions.postValue(currentSessions.toList())

        currentSessions.forEach { session ->
            verifySingleSession(context, session.fileName)
        }
    }

    private fun verifySingleSession(context: Context, fileName: String) {
        scope.launch {
            log("Проверка сессии $fileName...")
            delay(1000)
            val newStatus = if (Math.random() > 0.3) VerificationStatus.VALID else VerificationStatus.INVALID
            updateSessionStatus(fileName, newStatus)
        }
    }

    fun sendMessagesFromAllSessions(context: Context, target: String, message: String, count: Int, delay: Float) {
        val validSessions = _sessions.value?.filter { it.status == VerificationStatus.VALID }
        if (validSessions.isNullOrEmpty()) {
            log("Нет валидных сессий для отправки.")
            return
        }
        log("🚀 Запуск рассылки с ${validSessions.size} сессий...")
        validSessions.forEach { session ->
            log("Отправка с ${session.fileName}...")
        }
    }

    private fun updateSessionStatus(fileName: String, status: VerificationStatus) {
        val currentList = _sessions.value?.toMutableList() ?: return
        val sessionIndex = currentList.indexOfFirst { it.fileName == fileName }
        if (sessionIndex != -1) {
            currentList[sessionIndex].status = status
            _sessions.postValue(currentList)
        }
    }

    private fun saveSessionFile(phone: String) {
        scope.launch {
            try {
                val sessionDir = File(appContext.filesDir, "sessions")
                if (!sessionDir.exists()) {
                    sessionDir.mkdirs()
                }
                val sanitizedName = phone.replace(Regex("[^0-9]"), "")
                val sessionFile = File(sessionDir, "+${sanitizedName}.session")
                if (!sessionFile.exists()) {
                    val activeSessionDir = File(appContext.filesDir, "tdlib/database/1/session")
                    if (activeSessionDir.exists()) {
                        activeSessionDir.copyTo(sessionFile, true)
                        log("✅ Сессия для $phone сохранена.")
                        loadSessions(appContext)
                    } else {
                        log("❌ Не удалось найти активную сессию для сохранения.")
                    }
                }
            } catch (e: Exception) {
                log("❌ Ошибка сохранения файла сессии: ${e.message}")
            }
        }
    }
}