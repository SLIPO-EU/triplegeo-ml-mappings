# triplegeo-ml-mappings

Receives as input a csv file and maps its column to the ontology of slipo. Functionality implemented in FieldMatcher object.


Functions of FieldMatcher to use:

public void makeModels(String inFolderPath, String outModelsPath):

Reads all .yml .csv from inFolderPath. In the folder, each .yml should be matche to a .csv.

Serializes a FieldMatcher object containinf the trained classifiers to outModelsPath.

public Mappings giveMatchings(String csvPath):

Receives the path to a .csv. Produces a Mappings object with the mappings for each column of the .csv.

Mappings object is described in Mappings.java

