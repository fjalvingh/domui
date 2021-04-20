/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/
job("Build and run tests") {
    container(displayName = "Run mvn install", image = "markhobson/maven-chrome:jdk-11") {
        shellScript {
            content = """
	            mvn clean install
            """
        }
    }
}

//job("Build and run tests") {
//    container(displayName = "Run mvn install", image = "maven:3.8.1-jdk-11") {
//        service("chromedp/headless-shell:latest") {
//            alias("browser")
//        }
//        shellScript {
//            content = """
//	            mvn clean install -DskipITs=true -Djetty.skip=true
//            """
//        }
//    }
//}

