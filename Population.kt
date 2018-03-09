import java.util.*
import com.fasterxml.uuid.Generators
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import kotlin.collections.LinkedHashMap

interface IPopulation
{
    fun dailyRoutine()
    fun hourlyRoutine(tick: Long)
    fun consumeResource(person: Long)
    fun workingOn(task: UUID)
    fun lookingForNewTask(idlePerson: UUID)
    fun reassignTask()
}

class Population(val eb: EventBus) : IPopulation, GUI_population, Initializer
{
    var names = LinkedHashMap<UUID, String>()
    var assignedTask = LinkedHashMap<UUID, UUID>()
    var workingHours = LinkedHashMap<UUID, Array<Long>>()
    var availableHours: Map<UUID, Long> = mutableMapOf()
    var requiredResource = LinkedHashMap<UUID, Long>()

    init
    {
        newPerson("bob", 10, 4)
        newPerson("alice", 8, 9)

        var person = UUID.fromString("522b276a-356b-5f39-813d-fabea2cd43e1") // alice
        var task = UUID.fromString("3b6a7a00-9603-51ff-907c-9c329a0bb4b3") // firstTask
        assignedTask.put(person, task)

        availableHours = workingHours.mapValues { it.value[0] + it.value[1] }

        reassignTask()
    }

    override fun dailyRoutine()
    {
        requiredResource.forEach { person ->
            consumeResource(person.value)
            // TODO this is obviously nonsense, task creation should be driven by some perceived need / player input
            for (i in 1..2) eb.send("newTask", "")
        }
    }

    override fun hourlyRoutine(tick: Long)
    {
        workingHours.forEach { person ->
            var wakeup = person.value[0]
            if (tick >= wakeup && tick <= availableHours[person.key]!!)
            {
                when
                {
                    assignedTask.contains(person.key) -> workingOn(assignedTask[person.key]!!).also {
                        eb.send(Address.DEBUG.name, "${names[person.key]} is working on ${assignedTask[person.key]}")
                    }
                    else -> lookingForNewTask(person.key)
                }
            }
        }
    }

    override fun workingOn(task: UUID)
    {
        eb.send("inProgress", "$task")
    }

    override fun consumeResource(person: Long)
    {
        eb.send("nomnom", person.toString())
    }

    override fun reassignTask()
    {
        eb.consumer<Any>("reassign", { oldtask ->
            var idlePerson = assignedTask.filter { person -> person.value == asUUID(oldtask) }.keys.toTypedArray()[0]
            assignedTask.remove(idlePerson)
            lookingForNewTask(idlePerson)
        })
    }

    override fun lookingForNewTask(idlePerson: UUID)
    {
        eb.send<Any>("LookingForStuffToDo", idlePerson.toString(), { newTask ->
            if (newTask.succeeded()) assignedTask.put(idlePerson, asUUID(newTask))
        })
    }

    fun newPerson(name: String, wakeup: Long, laborHours: Long)
    {
        var uuid = Generators.nameBasedGenerator().generate(name)
        names.put(uuid, name)
        workingHours.put(uuid, arrayOf(wakeup, laborHours))
        requiredResource.put(uuid, 8)
        println("Hello, Im $name! but my friends call me $uuid, I wake up at $wakeup")
    }

    fun newMockPerson()
    {
        var wakeup = Random().nextInt(6) + 3L
        var laborHours = Random().nextInt(8) + 6L
        var uuid = Generators.randomBasedGenerator().generate()
        workingHours.put(uuid, arrayOf(wakeup, laborHours))
        availableHours = workingHours.mapValues { it.value[0] + it.value[1] }
        requiredResource.put(uuid, 8)
    }

    override fun setInitialConditions(json: JsonObject)
    {
        TODO("not implemented")
    }

    override fun getTotalAlive() : Int
    {
        return names.count()
    }
}


