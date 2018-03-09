import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.util.*

enum class Address(val eb: String)
{
    SIMULATOR("simulator"), CLOCK("clock"), DEBUG("log")
}

interface Initializer
{
    fun setInitialConditions(json : JsonObject)
}

/*
utility functions for messaging over event bus
 */
fun asUUID(m: Message<Any>): UUID
{
    return UUID.fromString(m.body().toString())
}

fun asUUID(ar: AsyncResult<Message<Any>>): UUID
{
    return UUID.fromString(ar.result().body().toString())
}

fun asLong(m: Message<Any>): Long
{
    return m.body().toString().toLong()
}

fun asInt(m: Message<Any>): Int
{
    return m.body().toString().toInt()
}

fun asInt(ar: AsyncResult<Message<Any>>): Int
{
    return ar.result().body().toString().toInt()
}