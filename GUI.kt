interface GUI_population
{
    fun getTotalAlive() : Int
}

interface GUI_simulator
{
    fun getYear()
}

class GUI(val pop: GUI_population, val sim: GUI_simulator) : GUI_population, GUI_simulator
{
    override fun getYear()
    {
        sim.getYear()
    }

    override fun getTotalAlive() : Int
    {
         return pop.getTotalAlive()
    }
}

