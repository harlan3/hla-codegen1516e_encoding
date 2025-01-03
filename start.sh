
# Add the required jars to classpath
CP=lib/commons-io-2.15.1.jar:lib/commons-lang3-3.0.jar:lib/commons-text-1.10.0.jar:lib/derby.jar:lib/derbyclient.jar:lib/derbyshared.jar:lib/hla_path_builder.jar:dist/hla_1516e_encoding.jar

java -cp $CP orbisoftware.hla_codegen1516e_encoding.codeGenerator.MainApplication $@