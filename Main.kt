import io.vertx.core.*

//TODO replace all magic strings used for event bus addresses with enums

/*
when running mock simulation two bugs popped up ~ 7-3-2018
- SEVERE: Failed to handleMessage. address: inProgress java.util.ConcurrentModificationException
    at Task$1.handle(Task.kt:77)
    at Task$1.handle(Task.kt:15)
-SEVERE: Failed to handleMessage. address: reassign java.lang.ArrayIndexOutOfBoundsException: 0
    at Population$4.handle(Population.kt:107)
    at Population$4.handle(Population.kt:14)
 */

/*
due to the nature of Processing's PApplet as companion object, dependency injection is not working as it should for Simid class
as an alternative DependencyContainer holds all object to be initialized, and only has few methods which are called inside Processing
furthermore only PlayerInput & GUI class should be public, i.e. simulator logic SHOULD NOT be accessible in Processing
the container itself gets initialized by Simid class
*/

class DependencyContainer
{
    val vertx = Vertx.vertx()!!
    val eb = vertx.eventBus()!!
    var input = PlayerInput(eb)


    private var tasks: ITask = Task(eb)
    private var resources: IResourceManager = ResourceManager(eb)
    private var population: IPopulation = Population(eb)
    private val simulator: ISimulator = Simulator(input, population, eb)

    var gui = GUI(population as GUI_population, simulator as GUI_simulator)

    fun runMock()
    {
        simulator.runMock()
    }

    fun run()
    {
        simulator.run()
    }
}


fun main(args: Array<String>)
{
    Simid.P5.main(args)
}









































