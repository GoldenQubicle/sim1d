import com.fasterxml.uuid.Generators
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import java.util.*
import kotlin.collections.LinkedHashMap

interface ITask
{
    fun result(fromTask: Double)
    fun decommission(taskAssigned: UUID)
    fun getNewTask()
    fun taskInProgress()
    fun createNewTask()
}

class Task(val eb: EventBus) : ITask, Initializer
{
    var tasks = LinkedHashMap<UUID, MessageConsumer<Any>>()
    var hoursToComplete = LinkedHashMap<UUID, Long>()
    var resourceProduced = LinkedHashMap<UUID, Double>()
    var inProgress = mutableListOf<UUID>()
    var ratioHoursResult = 1.25

    init
    {
        newTask(Generators.nameBasedGenerator().generate("firstTask"), 4)
        newTask(Generators.randomBasedGenerator().generate(), 5)
        newTask(Generators.randomBasedGenerator().generate(), 15)

        taskInProgress()
        getNewTask()
        createNewTask()
    }

    override fun taskInProgress()
    {
        eb.consumer<Any>("inProgress", { taskAssigned ->
            if (!inProgress.contains(asUUID(taskAssigned))) inProgress.add(asUUID(taskAssigned))
            hoursToComplete.entries.forEach { task ->
                if (task.key == asUUID(taskAssigned))
                {
                    task.setValue(task.value - 1)
                    eb.send(Address.DEBUG.name, "tasks $task remaining ${task.value}")
                    if (task.value == 0L)
                    {
                        tasks.remove(asUUID(taskAssigned))
                        inProgress.remove(asUUID(taskAssigned))
                        result(resourceProduced[asUUID(taskAssigned)]!!)
                        decommission(asUUID(taskAssigned))
                    }
                }
            }
        })
    }

    override fun result(fromTask: Double)
    {
        eb.send("taskResult", fromTask.toString())
    }

    override fun decommission(taskAssigned: UUID)
    {
        eb.send("reassign", taskAssigned.toString())
    }

    override fun getNewTask()
    {
        eb.consumer<Any>("LookingForStuffToDo", { idlePerson ->
            var todoTask = tasks.keys.filter { tasks -> !inProgress.contains(tasks) }.toCollection(mutableListOf())
            if (todoTask.isNotEmpty())
            {
                inProgress.add(todoTask.first())
                idlePerson.reply(todoTask.first().toString())
            }
        })
    }

    override fun createNewTask()
    {
        eb.consumer<Any>("newTask", {
            var hours = Random().nextInt(15) + 10L
            newTask(Generators.randomBasedGenerator().generate(), hours)
        })
    }

    fun newTask(uuid: UUID, hoursConsumed: Long)
    {
        tasks.put(uuid, eb.consumer<Any>(uuid.toString()))
        hoursToComplete.put(uuid, hoursConsumed)
        resourceProduced.put(uuid, hoursConsumed * ratioHoursResult)
        //        println("tasks takes $hoursConsumed hours to complete and has id $uuid")
    }

    override fun setInitialConditions(json: JsonObject)
    {
        TODO("not implemented")
    }

}

