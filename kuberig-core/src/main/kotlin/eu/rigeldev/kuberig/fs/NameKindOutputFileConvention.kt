package eu.rigeldev.kuberig.fs

import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import java.io.File

class NameKindOutputFileConvention : OutputFileConvention {

    override fun outputFile(outputDirectory: File, resourceGeneratorMethod: ResourceGeneratorMethod, yaml: String): File {
        val yamlMap : Map<String, Any> = Yaml().load(yaml)
        val jsonObject = JSONObject(yamlMap)
        val kind = jsonObject.getString("kind")
        val name = jsonObject.getJSONObject("metadata").getString("name")

        return File(outputDirectory, "${name}_${kind}.yaml")
    }
}