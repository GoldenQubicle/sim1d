import io.vertx.core.eventbus.EventBus
import java.util.*

interface IPlayerInput
{
    val eb: EventBus
    var pause: Boolean
}

class PlayerInput(_eb: EventBus) : IPlayerInput
{
    override val eb: EventBus = _eb
    override var pause: Boolean = true

    fun newTask()
    {
        eb.send("newTask", "")
    }
}
