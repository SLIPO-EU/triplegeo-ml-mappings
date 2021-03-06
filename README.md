# triplegeo-ml-mappings

Receives as input a csv file and maps its column to the ontology of SLIPO. Functionality implemented in [FieldMatcher](src/main/java/eu/slipo/triplegeo/ml/mappings/FieldMatcher.java) object.


## FieldMatcher Functions

```java
public void makeModels(String inFolderPath, String outModelsPath)
```

Reads all .yml and .csv from `inFolderPath`. In the folder, each .yml mapping file should be matched to a .csv data file

Serializes a [FieldMatcher](src/main/java/eu/slipo/triplegeo/ml/mappings/FieldMatcher.java) object containing the trained classifiers to `outModelsPath`.

```java
public Mappings giveMatchings(String csvPath)
```

Receives the path to a .csv file. Produces a [Mappings](src/main/java/eu/slipo/triplegeo/ml/mappings/Mappings.java) object with the mappings for each column of the .csv file.


## Build

To build the project:

`mvn clean install`

## Run

To create model:

`java -jar target/field-matcher.jar -d "<path to data and mapping files>" -o "<path to model file>"`

To compute field mappings:

`java -jar target/field-matcher.jar -m "<path to model file>" -f "<path to input data file>"`
