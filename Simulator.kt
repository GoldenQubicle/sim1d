import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject

interface ISimulator
{
    val input: IPlayerInput
    var population: IPopulation
    val gameTick: Long
    var startTick: Long
    var pauseTick: Long
    var isRunning: Boolean
    fun run()
    fun runMock()
    fun daily()
    fun hourly()
}

class Simulator(override val input: IPlayerInput, override var population: IPopulation, val eb: EventBus) : ISimulator, GUI_simulator, Initializer
{
    override var isRunning = false
    override val gameTick: Long = 5000
    //TODO : take pause into account
    override var startTick: Long = 0
    override var pauseTick: Long = 0
    var counter = 0
    var clock: Long = 0
    val day: MessageConsumer<Any> = eb.consumer<Any>(Address.SIMULATOR.name)
    val hour: MessageConsumer<Any> = eb.consumer<Any>(Address.CLOCK.name)

    init
    {
        daily()
        hourly()
    }

    override fun daily()
    {
        day.handler {
            population.dailyRoutine()
        }
    }

    override fun hourly()
    {
        hour.handler { tick ->
            population.hourlyRoutine(asLong(tick))
        }
    }

    override fun getYear()
    {
        TODO("not implemented")
    }

    override fun setInitialConditions(json: JsonObject)
    {
        TODO("not implemented")
    }

    override fun run()
    {
        isRunning = !input.pause
        if (isRunning)
        {
            if (startTick == 0L)
            {
                startTick = System.currentTimeMillis()
            }

            if (System.currentTimeMillis() > startTick + gameTick)
            {
                startTick = System.currentTimeMillis()
                counter++
                eb.publish(Address.SIMULATOR.name, counter)
            }
            // probably not the most elegant solution but it'll do xD
            var timeInDay = (startTick + gameTick) - System.currentTimeMillis()
            if ((24 + timeInDay * (-24) / gameTick) != clock)
            {
                clock = 24 + timeInDay * (-24) / gameTick
                eb.publish(Address.CLOCK.name, clock)
            }

        }
    }

    override fun runMock()
    {
        for (i in 0..365)
        {
            counter++
            eb.publish(Address.SIMULATOR.name, counter)
            for (j in 0..24)
            {
                clock = j.toString().toLong()
                eb.publish(Address.CLOCK.name, clock)
            }
        }
    }
}