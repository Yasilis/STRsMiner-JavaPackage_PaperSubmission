
rm(list=ls())

library(biomaRt)

# dataDirectory = "F:/MyLibraries/Documents/TemplateCodes/Codes/RStudio/PGM/Main-Project/Raw_Data"
dataDirectory <- "F:/MyLibraries/Documents/TemplateCodes/Codes/IdeaProjects/STRsMiner1.0.0.3/assets"

listEnsembl()

ensembl <- useEnsembl(biomart="ensembl")
datasets <- listDatasets(ensembl)
dim(datasets)

write.table(datasets, paste(dataDirectory, "/GeneDatasetNameList_DB_V_3.txt" ,sep = ""), sep = "$", quote = FALSE, row.names = FALSE)
