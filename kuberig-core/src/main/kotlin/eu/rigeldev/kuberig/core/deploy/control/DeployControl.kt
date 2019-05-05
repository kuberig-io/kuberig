package eu.rigeldev.kuberig.core.deploy.control

import java.time.Duration

class DeployControl {

    var tickRange = IntRange.EMPTY
    var tickDuration: Duration = Duration.ofSeconds(10)
    var tickGateKeeper: String = "eu.rigeldev.kuberig.core.deploy.control.DefaultTickGateKeeper"

}