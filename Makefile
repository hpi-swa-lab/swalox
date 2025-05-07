# Just for convenience....

run: classpath
	./lox

test:
	./mvnw -e clean test

classpath: force
	./mvnw -q exec:exec -Dexec.executable=echo -Dexec.args="%classpath" > classpath

native:
	./mvnw -P native package


force:
