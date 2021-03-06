package com.soywiz.korge.service.storage

open class InmemoryStorage : IStorage {
    val data = LinkedHashMap<String, String>()
    override operator fun set(key: String, value: String): Unit = run { data[key] = value }
    override fun getOrNull(key: String): String? = data[key]
    override fun remove(key: String): Unit = run { data.remove(key) }
    override fun removeAll(): Unit = data.clear()
}
