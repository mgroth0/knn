import matt.kbuild.settings.applySettings
import matt.mbuild.applyMSettings


buildscript {

  val props = java.util.Properties().apply {
	load(
	  sourceFile!!.parentFile.resolve("gradle.properties").reader()
	)
  }

  fun prop(key: String) = (gradle.startParameter.projectProperties[key] ?: props[key].toString())
  val VERBOSE = prop("verboseLogging").toBoolean()
  if (VERBOSE) println("top of settings.gradle.kts buildscript block")
  repositories {
	mavenLocal()
	mavenCentral()
	google()
	gradlePluginPortal()
	maven(
	  url = "https://s01.oss.sonatype.org/content/repositories/releases/"
	)
  }/*this is necessary for libs.xmlutil.core and libs.xmlutil.serialization*/
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
	  "kbuild", "mbuild"
	).forEach { gradleMod ->

	  val kbuildDir = registeredDir.resolve("gbuild/dist/$gradleMod")
	  val numBack = prop("NUM_BACK").toInt()
	  if (osName == "Windows 11") {
		classpath(files("Y:\\$gradleMod.jar")) /*PROBABLY WONT WORK AFTER KBUILD DEPS LIST FILE UPDATE*/
	  } else if (prop(
		  "PARTIAL_BOOTSTRAP"
		).toBoolean()
	  ) {        //	  classpath(files(registeredDir.resolve("kbuild.jar"))) /*PROBABLY WONT WORK AFTER KBUILD DEPS LIST FILE UPDATE*/
		classpath(
		  files(registeredDir.resolve("gbuild/jar/$gradleMod.jar"))
		) /*PROBABLY WONT WORK AFTER KBUILD DEPS LIST FILE UPDATE*/
	  } else {
		val kbuildLibsFolder = if (numBack == 0) registeredDir.resolve("bin/dist/$gradleMod/lib")
		else {
		  val recentVersion = kbuildDir.list()!!.mapNotNull {
			it.toLongOrNull()
		  }.sorted().reversed()[numBack]
		  kbuildDir.resolve("$recentVersion")
		}
		classpath(fileTree(kbuildLibsFolder))
		val deps = kbuildLibsFolder.resolve("deps.txt").readLines().filter { it.isNotBlank() }.map {
		  val parts = it.split(":")
		  Dep(parts[0], parts[1], parts[2])
		}


		deps.forEach {
		  val thisDep = it
		  depsSeen.firstOrNull { it.group == thisDep.group && it.name == thisDep.name }?.let {
			require(it.version == thisDep.version) {
			  "conflicting versions for ${thisDep.group}:${thisDep.name}"
			}
		  } ?: run {
			classpath(thisDep.toString())
			depsSeen += thisDep
		  }
		}
	  }
	}


  }
  if (VERBOSE) println("bottom of settings.gradle.kts buildscript block")
}

applySettings()
applyMSettings()