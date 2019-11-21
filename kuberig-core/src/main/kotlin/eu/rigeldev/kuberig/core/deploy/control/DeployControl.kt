package eu.rigeldev.kuberig.core.deploy.control

import java.time.Duration

class DeployControl {

    var tickRangeStart : Int = 1
    var tickRangeEnd: Int = 1
    var tickDuration: Duration = Duration.ofSeconds(10)
    var tickGateKeeper: String = "eu.rigeldev.kuberig.core.deploy.control.DefaultTickGateKeeper"

}