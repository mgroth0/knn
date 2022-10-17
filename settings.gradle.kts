import matt.kbuild.settings.applySettings


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

	listOf(
	  "kbuild"
	).forEach { gradleMod ->

	  //	val kbuildDir = registeredDir.resolve("kbuild")
	  val kbuildDir = registeredDir.resolve("gbuild/dist/$gradleMod")
	  val numBack = prop("NUM_BACK").toInt()
	  if (osName == "Windows 11") {
		classpath(files("Y:\\$gradleMod.jar")) /*PROBABLY WONT WORK AFTER KBUILD DEPS LIST FILE UPDATE*/
	  } else if (prop("PARTIAL_BOOTSTRAP").toBoolean()) {
		//	  classpath(files(registeredDir.resolve("kbuild.jar"))) /*PROBABLY WONT WORK AFTER KBUILD DEPS LIST FILE UPDATE*/
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
		val deps = kbuildLibsFolder.resolve("deps.txt").readLines().filter { it.isNotBlank() }
		/*val depsTxt = gradle.rootProject.projectDir.resolve("deps.txt")
		require(!depsTxt.exists())
		depsTxt.writeText(deps.joinToString("\n"))*/
		deps.forEach {
		  classpath(it)
		}
	  }
	}

  }
  if (VERBOSE) println("bottom of settings.gradle.kts buildscript block")
}

/*TODO: somehow send plugin version info into the next function*/
/*val depsTxt = gradle.rootProject.projectDir.resolve("deps.txt")
val s = depsTxt.readText()
depsTxt.delete()*/
applySettings()