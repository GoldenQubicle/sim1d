import processing.core.*

class Simid : PApplet()
{
    companion object P5
    {
        fun main(args: Array<String>)
        {
            PApplet.main("Simid")
        }
    }

    override fun settings()
    {
        size(512, 512, PConstants.P2D)
    }

    val dc = DependencyContainer()
    var counter = 0
    var clock = ""
    var logs = ArrayList<String>()


    override fun setup()
    {
        dc.eb.consumer<Int>(Address.SIMULATOR.name, { message ->
            counterUpdate(message.body())
        })

        dc.eb.consumer<String>(Address.DEBUG.name, { message ->
            logsUpdate(message.body())
        })

        dc.eb.consumer<Long>(Address.CLOCK.name, { message ->
            clockUpdate(message.body().toString())
        })

        for (i in 0..20)
        {
            logs.add("")
        }
    }

    var bg = 128


    override fun draw()
    {
        background(bg)
        dc.run()
        logsDisplay()
        counterDisplay()
        clockDisplay()
        text("fps:${frameRate.toInt()}", width - 120F, 10F)
        text(dc.gui.getTotalAlive(), width - 150F, 10F)

    }

    override fun keyPressed()
    {
        if (key == 'p') dc.input.pause = !dc.input.pause

        if (key == 't') dc.input.newTask()

        if (key == 'm') dc.runMock()
    }

    fun logsDisplay()
    {
        var y = 10F
        for (i in 0..20)
        {
            text(logs[i], 15F, y + 10F * i)
        }
    }

    fun logsUpdate(message: String)
    {
        for (i in 0..19)
        {
            logs[i] = logs[i + 1]
        }
        logs[20] = message
    }

    fun clockDisplay()
    {
        text("time $clock", width - 60F, 20F)
    }

    fun clockUpdate(message: String)
    {
        clock = message
    }

    fun counterDisplay()
    {
        text("day $counter", width - 60F, 10F)
    }

    fun counterUpdate(message: Int)
    {
        counter = message
    }


}