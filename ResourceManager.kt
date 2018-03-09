import io.vertx.core.eventbus.EventBus

interface IResourceManager
{

}

class ResourceManager(val eb: EventBus) : IResourceManager
{
    var resources: Double = 32.0

    init
    {
        eb.consumer<Any>("taskResult", { resourceProduced ->
            resources += resourceProduced.body().toString().toDouble()
            eb.send(Address.DEBUG.name, "produced, currently $resources of something available")

        })

        eb.consumer<Any>("nomnom", { resourceConsumed ->
            if (resourceConsumed.body().toString().toDouble() > resources) eb.send(Address.DEBUG.name, "~~~* *! ! ! G A M E O V E R ! ! !* *~~~")
            else resources -= resourceConsumed.body().toString().toDouble()
            eb.send(Address.DEBUG.name, "consumed, currently $resources of something available")
        })

    }
}