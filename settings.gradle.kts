import matt.kbuild.settings.applySettings
import matt.mbuild.applyMSettings
import matt.mbuild.inspect.applyInspectSettings


buildscript {

  val props = java.util.Properties().apply {
	load(
	  sourceFile!!.parentFile.resolve("gradle.properties").reader()
	)
  }

  fun prop(key: String) = (gradle.startParameter.projectProperties[key] ?: props[key].toString())
  val verbose = prop("verboseLogging").toBoolean()
  if (verbose) println("top of settings.gradle.kts buildscript block")
  repositories {
	mavenLocal()
	mavenCentral()
	google()
	gradlePluginPortal()
	maven(
	  url = "https://s01.oss.sonatype.org/content/repositories/releases/"
	)
  }
  /*this is necessary for libs.xmlutil.core and libs.xmlutil.serialization*/
  val androidAttribute = Attribute.of("net.devrieze.android", Boolean::class.javaObjectType)
  configurations.all {
	attributes {
	  attribute(androidAttribute, false)
	}
  }
  dependencies {


	val osName = System.getProperty("os.name")
	val userHomeFolder = File(System.getProperty("user.home"))
	val registeredDir = userHomeFolder.resolve("registered")

	class Dep(val group: String, val name: String, val version: String) {
	  override fun toString(): String {
		return "$group:$name:$version"
	  }
	}

	val depsSeen = mutableListOf<Dep>()



	listOf(
	  "kbuild",
	  "inspect",
	  "mbuild",

	  ).forEach { gradleMod ->

	  val kbuildDir = registeredDir.resolve("gbuild/dist/$gradleMod")
	  val numBack = prop("NUM_BACK").toInt()
	  if (osName == "Windows 11") {
		classpath(files("Y:\\$gradleMod.jar")) /*PROBABLY WONT WORK AFTER KBUILD DEPS LIST FILE UPDATE*/
	  } else {
		val recentVersion = kbuildDir.list()!!.mapNotNull {
		  it.toLongOrNull()
		}.sorted().reversed()[numBack]
		val kbuildLibsFolder = kbuildDir.resolve("$recentVersion")
		classpath(fileTree(kbuildLibsFolder))
		val deps = kbuildLibsFolder.resolve("deps.txt").readLines().filter { it.isNotBlank() }.map {
		  val parts = it.split(":")
		  Dep(parts[0], parts[1], parts[2])
		}


		deps.forEach { dep ->
		  depsSeen.firstOrNull { it.group == dep.group && it.name == dep.name }?.let {
			require(it.version == dep.version) {
			  "conflicting versions for ${dep.group}:${dep.name}"
			}
		  } ?: run {
			classpath(dep.toString())
			depsSeen += dep
		  }
		}
	  }
	}


  }
  if (verbose) println("bottom of settings.gradle.kts buildscript block")
}

applySettings()
applyInspectSettings()
applyMSettings()