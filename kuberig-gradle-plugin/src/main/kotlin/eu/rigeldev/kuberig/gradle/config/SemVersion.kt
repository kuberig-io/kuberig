package eu.rigeldev.kuberig.gradle.config

class SemVersion(val majorVersion: Int, val minorVersion: Int, val patchVersion: Int) {
    fun isEqual(otherSemVersion: SemVersion): Boolean {
        return this.majorVersion == otherSemVersion.majorVersion
                && this.minorVersion == otherSemVersion.minorVersion
                && this.patchVersion == otherSemVersion.patchVersion
    }

    fun isHigher(otherSemVersion: SemVersion): Boolean {
        if (majorVersion > otherSemVersion.majorVersion) {
            return true
        } else if (majorVersion == otherSemVersion.majorVersion) {
            if (minorVersion > otherSemVersion.minorVersion) {
                return true
            } else if (minorVersion == otherSemVersion.minorVersion) {
                return patchVersion > otherSemVersion.patchVersion
            }
        }

        return false
    }

    override fun toString(): String {
        return "$majorVersion.$minorVersion.$patchVersion"
    }


    companion object {
        fun fromVersionText(versionText: String): SemVersion {
            val parts = versionText.split('.')

            check(parts.size == 3) { "$versionText is not a sem-version, does not have 3 parts separated by periods."}

            try {
                return SemVersion(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
                )
            }
            catch (e: NumberFormatException) {
                throw IllegalStateException("Not all parts in $versionText are numbers", e)
            }
        }
    }
}