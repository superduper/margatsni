clean: 
	rm -rf dist/bin/*
	sbt clean

dist:
	sbt assembly
	cp margatsni-bootstrap-server/target/scala-2.10/margatsni-bootstrap-server.jar
	cp margatsni-node/target/scala-2.10/margatsni-node.jar
