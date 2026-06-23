package com.bbv.base.util

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * 日志工具
 *
 * 支持特性：
 * - 长日志自动分段（Android Logcat 单条上限 ~4000 字符）
 * - Json 自动格式化
 * - 异常栈打印
 * - RELEASE 模式自动关闭
 * - 自动追踪调用类生成 tag
 */
object Lg {

    private const val DEFAULT_TAG = "AppLog"
    private const val MAX_LOG_LENGTH = 4000

    /** 是否启用日志（RELEASE 时设为 false） */
    var isDebug = true

    // ==================== 快捷方法 ====================

    fun d(msg: String, tag: String = currentTag()) {
        logLong(tag, msg, LogLevel.DEBUG)
    }

    fun i(msg: String, tag: String = currentTag()) {
        logLong(tag, msg, LogLevel.INFO)
    }

    fun w(msg: String, tag: String = currentTag()) {
        logLong(tag, msg, LogLevel.WARN)
    }

    fun e(msg: String, tag: String = currentTag()) {
        logLong(tag, msg, LogLevel.ERROR)
    }

    /** 打印异常（含栈信息） */
    fun e(tr: Throwable?, tag: String = currentTag()) {
        if (tr == null) return
        logLong(tag, Log.getStackTraceString(tr), LogLevel.ERROR)
    }

    fun e(msg: String, tr: Throwable?, tag: String = currentTag()) {
        logLong(tag, "$msg\n${Log.getStackTraceString(tr)}", LogLevel.ERROR)
    }

    /** 打印 Json（自动格式化） */
    fun json(json: String?, tag: String = currentTag()) {
        if (json.isNullOrBlank()) {
            logLong(tag, "json: empty/null", LogLevel.DEBUG)
            return
        }
        val formatted = try {
            when {
                json.startsWith("{") -> JSONObject(json).toString(2)
                json.startsWith("[") -> JSONArray(json).toString(2)
                else -> json
            }
        } catch (_: Exception) {
            json
        }
        logLong(tag, formatted, LogLevel.DEBUG)
    }

    // ==================== 核心方法 ====================

    /**
     * 打印长日志（自动分段）
     * @param tag 标签
     * @param message 日志内容
     * @param level 日志级别
     */
    @JvmOverloads
    fun logLong(
        tag: String = DEFAULT_TAG,
        message: String,
        level: LogLevel = LogLevel.DEBUG
    ) {
        if (!isDebug) return
        if (message.length <= MAX_LOG_LENGTH) {
            logSingle(tag, message, level)
            return
        }
        message.lineSequence().forEach { line ->
            if (line.length > MAX_LOG_LENGTH) {
                chunkLog(tag, line, level)
            } else {
                logSingle(tag, line, level)
            }
        }
    }

    private fun chunkLog(tag: String, message: String, level: LogLevel) {
        var index = 0
        while (index < message.length) {
            val end = (index + MAX_LOG_LENGTH).coerceAtMost(message.length)
            logSingle(tag, message.substring(index, end), level)
            index = end
        }
    }

    private fun logSingle(tag: String, message: String, level: LogLevel) {
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, message)
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARN -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }
    }

    /** 自动获取调用类的简单类名作为 tag */
    private fun currentTag(): String {
        val stack = Throwable().stackTrace
        // stack[0] = currentTag(), stack[1] = d/i/e/w, stack[2] = 调用方
        val caller = stack.getOrNull(2) ?: return DEFAULT_TAG
        val className = caller.className
        return className.substringAfterLast('.').substringBefore('$')
    }

    enum class LogLevel { VERBOSE, DEBUG, INFO, WARN, ERROR }
}
