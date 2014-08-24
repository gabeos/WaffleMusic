name := """WaffleMusic"""

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++=
  Seq("org.scalatest" % "scalatest_2.11" % "2.1.3" % "test",
  "de.sciss" %% "scalacollider" % "1.12.0" withSources(),
  "com.scalarx" %% "scalarx" % "0.2.6" withSources()
  )

//-Djava.library.path="C:/Users/Gabriel Schubiner/Projects/WaffleMusic/src/main/resources"