# triplegeo-ml-mappings

Receives as input a csv file and maps its column to the ontology of slipo. Entry class is FieldMatcherExec, which can be called in the following two ways:

FieldMatcherExec.jar -train folder_in models

FieldMatcherExec.jar -predict models file_in.csv predictions out

-train receives a folder with csv files and their yaml mappings, and produces a file with serialized models

-predict receives a file with serialized models and a csv and produces a human readable text file with mappings, which is read by triple-geo 
