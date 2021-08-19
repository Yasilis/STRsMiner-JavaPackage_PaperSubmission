


rm(list=ls())

library(glmnet)             # Lasso
library(Matrix)             # make sparse matrix
library(factoextra)         # show PCA plot
library(ggplot2)
library(ggfortify)
library(corrplot)
library(RColorBrewer)
library (randomForest)      # random forest
library(caret)              # fold
library (gbm)               # boost
library(xgboost)            # xgBoost
library(InformationValue)   # missclasserror
library(tidyverse)          # eliminate duplications

# set the seed to make partitions reproducible
set.seed(1723)

#############################################     Functions    ####################################################

get.k.fold.index = function(data.row.num, k.fold)
{
    folds = createFolds(seq(1,nrow(data.row.num)), k = k.fold, list = TRUE, returnTrain = FALSE)
    return(folds)
}


###########################################################################################################
###########################################    start of Lasso     #########################################
###########################################################################################################
calculate.lasso = function(train.data, train.data.labels, test.data, test.data.labels, fold.number)
{
    lasso.lambda.object = cv.glmnet(x = train.data, y = as.matrix(train.data.labels)
                                    , lambda = (5^seq(-7,1,by = 0.1)))
    png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "lasso.bestlambda_fold_", fold.number,".png", sep = ""))
    plot(lasso.lambda.object, sub=paste("Best Lambda = ", lasso.lambda.object$lambda.min, sep = ""))
    dev.off()
    # print(lasso.lambda.object)
    # lasso.lambda.object$lambda.min
    
    # Note alpha=1 for lasso only and can blend with ridge penalty down to
    # alpha=0 ridge only.
    # lasso.fit = glmnet(x = train.data, y = as.matrix(train.data.labels), alpha = 1, family="binomial"
    #                    , lambda = lasso.lambda.object$lambda.min)
    lasso.fit = glmnet(x = train.data, y = as.matrix(train.data.labels), alpha = 1
                       , lambda = lasso.lambda.object$lambda.min)
    png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "lasso.fita_fold_", fold.number,".png", sep = ""))
    plot(lasso.fit)
    dev.off()
    
    lasso.predict = predict(lasso.fit, s = lasso.lambda.object$lambda.min, newx = test.data)
    error.continues = apply((lasso.predict - test.data.labels)^2, 2, mean)
    coefficient = coef(lasso.fit, s = lasso.lambda.object$lambda.min)
    misClassError = misClassError(lasso.predict > 5, test.data.labels > 5)
    confusion.matrix = table(lasso.predict > 5, test.data.labels > 5)
    sensitivity = sensitivity(lasso.predict > 5, test.data.labels > 5)
    specificity = specificity(lasso.predict > 5, test.data.labels > 5)
    
    result = list(lasso.fit, lasso.predict, coefficient, error.continues, misClassError, sensitivity, specificity, confusion.matrix)
    
    return(result)
}
###########################################################################################################
############################################    End of Lasso     ##########################################
###########################################################################################################


###########################################################################################################
#######################################    start of Random forest     #####################################
###########################################################################################################
calculate.random.forest = function(gen.whole.train.Data, gen.whole.test.Data, fold.number)
{
    randomForest.fit = randomForest(V0~. ,data = rbind(gen.whole.train.Data, gen.whole.test.Data) 
                                    ,subset = row.names(gen.whole.train.Data) 
                                    ,mtry = as.integer(sqrt(dim(gen.whole.train.Data)[2]-1))
                                    ,importance =TRUE)
    
    # yhat.randomForest = predict (randomForest.fit ,newdata = gen.whole.test.Data)
    # importance(randomForest.fit)
    png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "Feature Importance top fold "
                                                                   , fold.number,".png", sep = ""))
    varImpPlot(randomForest.fit, n.var = 20, main = "Feature Importance")
    dev.off()
    
    variable = row.names(randomForest.fit$importance)
    importance = randomForest.fit$importance[,1]
    png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "Feature Importance total fold "
                                                                   , fold.number,".png", sep = ""))
    ggplot(randomForest.fit$importance, aes(x=reorder(variable,importance), y=importance,fill=importance))+ 
        geom_bar(stat="identity", position="dodge")+ coord_flip()+
        ylab("Feature Importance")+
        xlab("")+
        ggtitle("Information Value Summary")+
        guides(fill=F)+
        scale_fill_gradient(low="red", high="blue")
    dev.off()
    
    randomForest.predict = predict(randomForest.fit, newdata = gen.whole.test.Data)
    error.continues = mean((randomForest.predict - gen.whole.test.Data$V0)^2)
    coefficient = importance(randomForest.fit)
    misClassError = misClassError(randomForest.predict > 5, gen.whole.test.Data$V0 > 5)
    confusion.matrix = table(randomForest.predict > 5, gen.whole.test.Data$V0 > 5)
    sensitivity = sensitivity(randomForest.predict > 5, gen.whole.test.Data$V0 > 5)
    specificity = specificity(randomForest.predict > 5, gen.whole.test.Data$V0 > 5)
    
    result = list(randomForest.fit, randomForest.predict, coefficient, error.continues, misClassError, sensitivity, specificity, confusion.matrix)
    
    return(result)
}
###########################################################################################################
##########################################    End of Random forest     ####################################
###########################################################################################################


###########################################################################################################
#########################################    start of Boosting     ########################################
###########################################################################################################
calculate.Boosting = function(train.data, train.data.labels, test.data, test.data.labels, fold.number)
{
    # xgboost.fit = xgboost(data = train.data, label = as.numeric(train.data.labels.factor$V0)-1, max.depth = 2, eta = 1, nrounds = 6000,
    #                       nthread = 4, objective = "binary:logistic")
    xgboost.fit1 = xgboost(data = train.data, label = train.data.labels$V0, max.depth = 2, eta = 1, nrounds = 1000,
                           nthread = 4, objective = "reg:squarederror")
    
    # predict
    xgboost.predict = predict(xgboost.fit1, test.data)
    coefficient = ""
    error.continues = apply((xgboost.predict - test.data.labels)^2, 2, mean)
    misClassError = misClassError(xgboost.predict > 5, test.data.labels > 5)
    confusion.matrix = table(xgboost.predict > 5, test.data.labels > 5)
    sensitivity = sensitivity(xgboost.predict > 5, test.data.labels > 5)
    specificity = specificity(xgboost.predict > 5, test.data.labels > 5)
    
    result = list(xgboost.fit1, xgboost.predict, coefficient, error.continues, misClassError, sensitivity, specificity, confusion.matrix)
    
    
    # importance_matrix = xgboost.fit(colnames(train.data), model=xgboost.fit)[0:20]
    
    
    return(result)
}


###################################################################################################################



#############################################     Read Data    ####################################################
saveAddress = "F:/MyLibraries/Documents/UniFolders/PHd/Courses/Machine Learning/Project/MainProject/ML Presentation/pic/"
dataFileAddress = "F:/MyLibraries/Documents/UniFolders/PHd/Courses/Machine Learning/Project/MainProject/"
# dataFileAddress = "D:/Ali-Maddi/Documents/Private/Uni Folder/ML/MainProject/"
# dataFileAddress = "D:/Ali-Maddi/Dropbox/For Amn/"

file.1 = "2019_11_27__12_36_20__sparse_matrix.csv"
file.row.name = "2020_01_05_rows_name.csv"
file.col.name = "2020_01_05_cols_name.csv"
file.sparse = "2020_01_05_sparse_matrix.mtx"


###################################################################################################################
########################################        Do for once         ###############################################
###################################################################################################################
# myData = read.csv(paste(dataFileAddress, file.1, sep = ""), header = TRUE, sep = "\t", check.names=FALSE)
# myDataLabels = myData$IDs
# # print(object.size(myData), units = "MB")
# # print(object.size(myDataLabels), units = "MB")
# write.csv(myDataLabels, file = paste(dataFileAddress, file.row.name, sep = ""), row.names = FALSE)
# write.csv(colnames(myData)[-1], file = paste(dataFileAddress, file.col.name, sep = ""), row.names = FALSE)
# 
# myDataLabels = as.character(myDataLabels)
# myDataLabels[which(startsWith(x = as.character(myData$IDs), prefix = "UFO"))] = "0"
# myDataLabels[which(!startsWith(x = as.character(myData$IDs), prefix = "UFO"))] = "1"
# 
# myData = cbind(myDataLabels, myData[,-1])
# colnames(myData)[1] = "V0"
# myData = myData[sample(nrow(myData)), ]
# # myData = Matrix(data.matrix(cbind(myDataLabels, myData[,-1])), sparse=TRUE)
# 
# # print(object.size(myData), units = "MB")
# writeMM(obj = myData, file=paste(dataFileAddress, file.sparse, sep = ""))
# rm(myData)
# rm(myDataLabels)
###################################################################################################################


myData = readMM(paste(dataFileAddress, file.sparse, sep = ""))
myDataLabels = myData[,1] - 1
data.col.names = read.csv(paste(dataFileAddress, file.col.name, sep = ""), header = FALSE, sep = "\t", check.names=FALSE)
data.row.names = read.csv(paste(dataFileAddress, file.row.name, sep = ""), header = FALSE, sep = "\t", check.names=FALSE)

data.col.names = as.character(data.col.names$V1)
data.col.names[1] = "label"
data.row.names = as.character(data.row.names$V1)
data.row.names = data.row.names[-1]
# length(myDataLabels)

# length(which(myDataLabels == '1'))
# length(which(myDataLabels == '0'))
myDataLabels = as.factor(myDataLabels)
# dim(myData)
# length(myDataLabels)
# head(myData[,1:10])
# head(myDataLabels[1:10])


# write.csv(myData, file = paste(dataFileAddress, "out__", file.1, sep = ""))




############################################     Explor The Data    ###############################################
# myData = myData[,which(apply(myData[, -1], 2, sum) > 5, arr.ind = TRUE)]
print(object.size(myData), units = "MB")
# Data = as.matrix(Data)

total.data = myData
total.data[,1]=total.data[,1]-1
# dim(total.data)
# dim(myData)
rm(myData)
rm(myDataLabels)


total.shuffle.data = total.data[sample(nrow(total.data)), ]
# head(shuffle.total.data)[,5760:5770]
# dim(shuffle.total.data)
rm(total.data)
# index.of.IDs = grep("^IDs$", colnames(shuffle.total.data))
# index.of.Labels = grep("^myDataLabels$", colnames(shuffle.total.data))
# shuffle.total.data[1:10,1:5]

head(total.shuffle.data)[,1:30]
head(data.col.names)
head(data.row.names)
dim(total.shuffle.data)
length(data.col.names)
length(data.row.names)


dimnames(total.shuffle.data) = list(data.row.names, data.col.names)
head(total.shuffle.data)[,1:30]


label.shuffle.data = total.shuffle.data[,1]
shuffle.data = total.shuffle.data[,-1]

dim(total.shuffle.data)
length(label.shuffle.data)
dim(shuffle.data)

###########
a = duplicated.matrix(t(shuffle.data))
table(a)
delete.col = names(a)[a]
remain.col = names(a)[!a]
length(remain.col)
shuffle.data = shuffle.data[, remain.col]
dim(shuffle.data)

total.shuffle.data = cbind(label.shuffle.data,shuffle.data)
colnames(total.shuffle.data)[1] = "label"
dim(total.shuffle.data)
head(total.shuffle.data)[,1:30]


bar.chart = rep(0,2)
bar.chart[1] = table(a)[2]
bar.chart[2] = table(a)[1]
png(height=150, width=140, units = "mm", res = 300, file=paste(saveAddress, "Duplicate Feature.png", sep = ""))
colors = c("green","orange","brown")
xx = barplot(bar.chart, main="Duplicated Features", xlab="Features", ylab = "Number of Features", names.arg = c("Duplicated","Unique") ,
        col = colors)
text(x = xx, y = bar.chart, label = bar.chart, pos = 1, cex = 1.2, col = "black")
dev.off()

delete.col[1:10]
remain.col[1:10]


bar.chart = rep(0,2)
bar.chart[1] = table(label.shuffle.data)[2]
bar.chart[2] = table(label.shuffle.data)[1]
png(height=150, width=140, units = "mm", res = 300, file=paste(saveAddress, "Frequency of Labels.png", sep = ""))
colors = c("green","orange","brown")
xx = barplot(bar.chart, main="Frequency of Labels", xlab="Labels", ylab = "Number of Labels", names.arg = c("Positive","Negative") ,
             col = colors)
text(x = xx, y = bar.chart, label = bar.chart, pos = 1, cex = 1.2, col = "black")
dev.off()


total.data = total.shuffle.data
rm(total.shuffle.data)
myData = total.data[,-1]
label = total.data[,1]
label.factor = as.factor(label)
total.data.factor = cbind(myData,label.factor)
total.data.factor = total.data
total.data.factor[,1] = as.factor(total.data[,1])
colnames(total.data.factor)[400] = colnames(total.data)[400]

pca = prcomp(myData[1:4000,], center = TRUE)
# pca2 = prcomp(myData[1:400,1:100], center = TRUE)

png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "prcomp.png", sep = ""))
autoplot(pca, data = as.data.frame(as.matrix(total.data[1:4000,])), colour = 'label',shape = 16)
# autoplot(pca2, data = total.data[1:400,1:100], colour = 'label',shape = 16)
dev.off()

png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "prcomp as factor.png", sep = ""))
autoplot(pca, data = as.data.frame(as.matrix(total.data[1:4000,])), colour = 'label',shape = 16)
dev.off()

png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "pca percentage.png", sep = ""))
fviz_eig(pca, ncp = 11, addlabels = TRUE, xlab = "PCA Component", linecolor = "red", main = "PCA Components and their Coverages")
dev.off()

png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "prcomp as factor2(1to2).png", sep = ""))
fviz_pca_ind(pca, col.ind = "groups", label = 'none', addEllipses = TRUE, ellipse.level = 0.95, axes = c(1,2)
             , habillage = total.data.factor[,400], repel = FALSE, palette = c("#E64A19",  "#303F9F"))
# col.ind = "groups"
# col.ind = "contrib"
# + scale_color_discrete(name = '') + theme(legend.direction = 'horizontal', legend.position = 'top')
dev.off()

png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "prcomp as factor2(2to3).png", sep = ""))
fviz_pca_ind(pca, col.ind = "groups", label = 'none', addEllipses = TRUE, ellipse.level = 0.95, axes = c(2,3)
             , habillage = total.data.factor[,400], repel = FALSE, palette = c("#E64A19",  "#303F9F"))
dev.off()

png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "prcomp as factor2(1to3).png", sep = ""))
fviz_pca_ind(pca, col.ind = "groups", label = 'none', addEllipses = TRUE, ellipse.level = 0.95, axes = c(1,3)
             , habillage = total.data.factor[,400], repel = FALSE, palette = c("#E64A19",  "#303F9F"))
dev.off()


corMatrix = cor(as.matrix(total.data[1:2000,]))
png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "corMatrix.png", sep = ""))
corrplot(corMatrix, method="ellipse", order="hclust", col = brewer.pal(n=8, name="PuOr"))
dev.off()




#$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$



















png(height=140, width=150, units = "mm", res = 300, file=paste(dataFileAddress, "Pic/", "prcomp.png", sep = ""))
PCA.result = prcomp(shuffle.data[1:4000,], center = TRUE)
autoplot(PCA.result, data = as.data.frame(as.matrix(total.shuffle.data[1:4000,])), colour = 'label',shape = 16)
dev.off()

# corMatrix = cor(shuffle.total.data[,which(apply(shuffle.total.data, 2, var) > 5 * 10^7, arr.ind = TRUE)])
corMatrix = cor(shuffle.data[1:2000,])
png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "corMatrix.png", sep = ""))
corrplot(corMatrix, method="ellipse", order="hclust", col = brewer.pal(n=8, name="PuOr"))
dev.off()


num = matrix(0,nrow = 1, ncol = 2)
colnames(num) = c(0,1)
for (i in 0:1) 
{
    Data = total.shuffle.data[which(total.shuffle.data[,"label"] == i),1]
    num[i+1] = length(Data)[1]
}
num
dev.off()
barplot(num, main="Frequency of Labels", xlab="Labels", ylab = "Number of Label")
###################################################################################################################


fold.number = 10
folds.index = get.k.fold.index(total.shuffle.data, fold.number)
results = matrix(0, nrow = fold.number, ncol = 14)
colnames(results) <- c("logistic","lda","qda","knn","svm.linear","svm.polynomial","svm.sigmoid", "radial"
                       , "anovadot", "rand.s", "rand.ns", "boost.s", "boost.ns", "xgboost.ns")

# colnames(shuffle.total.data) = as.character(data.col.names$V1)
# colnames(shuffle.total.data)[1] = "V0"











total.shuffle.data = total.data


fold.number = 10
folds.index = get.k.fold.index(total.shuffle.data, fold.number)

results = matrix(0, nrow = fold.number, ncol = 3)
colnames(results) <- c("lasso", "randomForest", "XGboost")

print(paste(0, " loop finished          ", Sys.time(), sep = "") )
i=1
for (i in 1:fold.number) 
{
    test.sample.indice = folds.index[[i]]
    
    primary.train.data = myData[-test.sample.indice, ]
    primary.test.data = myData[test.sample.indice, ]
    
    train.data.labels = data.frame(label[-test.sample.indice])
    colnames(train.data.labels) = c("V0")
    test.data.labels = data.frame(label[test.sample.indice])
    colnames(test.data.labels) = c("V0")
    
    train.data.labels.factor = data.frame(label.factor[-test.sample.indice])
    colnames(train.data.labels.factor) = c("V0")
    test.data.labels.factor = data.frame(label.factor[test.sample.indice])
    colnames(test.data.labels.factor) = c("V0")
    
    
    
    rm(test.sample.indice)
    
    
    ######################################################    Prepare data     ##############################################################
    train.data = model.matrix( V0~ 0 + ., data = cbind(train.data.labels,as.matrix(primary.train.data)))
    test.data = model.matrix( V0~ 0 + ., data = cbind(test.data.labels,as.matrix(primary.test.data)))
    train.data.factor = model.matrix( V0~ 0 + ., data = cbind(train.data.labels.factor,as.matrix(primary.train.data)))
    test.data.factor = model.matrix( V0~ 0 + ., data = cbind(test.data.labels.factor,as.matrix(primary.test.data)))
    #########################################################################################################################################
    
    
    rm(primary.train.data)
    rm(primary.test.data)
    
    
    
    # result = list(lasso.fit, lasso.predict, coefficient, error.continues, misClassError, sensitivity, specificity, confusion.matrix)
    lasso.result = calculate.lasso(train.data, train.data.labels, test.data, test.data.labels, i)
    results[i,1] = lasso.result[[5]]
    plot(lasso.result[[1]])
    
    xgboost.result = calculate.Boosting(train.data, train.data.labels, test.data, test.data.labels, i)
    results[i,3] = xgboost.result[[5]]
    
    gen.whole.train.Data = cbind(train.data.labels, train.data)
    gen.whole.test.Data = cbind(test.data.labels, test.data)
    
    rf.result = calculate.random.forest(gen.whole.train.Data, gen.whole.test.Data, i)
    results[i,2] = rf.result[[5]]
    
    
    
    print(paste(i, " loop finished          ", Sys.time(), sep = "") )
    print(results[i,])
}
results


















print(paste(0, " loop finished          ", Sys.time(), sep = "") )
i=1
for (i in 1:fold.number) 
{
    test.sample.indice = folds.index[[i]]
    
    primary.train.data = total.shuffle.data[-test.sample.indice,]
    primary.test.data = total.shuffle.data[test.sample.indice,]
    
    
    
    
    
    
    
    # primary.train.data.labels = data.frame(as.factor(shuffle.total.data[-test.sample.indice, 1]))
    # colnames(primary.train.data.labels) = c("V0")
    # primary.test.data.labels = data.frame(as.factor(shuffle.total.data[test.sample.indice, 1]))
    # colnames(primary.test.data.labels) = c("V0")
    # 
    # rm(test.sample.indice)
    
    
    ######################################################    Prepare data     ##############################################################
    # train.data.labels = set_binary_labels(primary.train.data.labels, 0, 0, 1)
    # colnames(train.data.labels) = c("V0")
    # test.data.labels = set_binary_labels(primary.test.data.labels, 0, 0, 1)
    # colnames(test.data.labels) = c("V0")
    # train.data.labels = primary.train.data.labels
    # test.data.labels = primary.test.data.labels
    # train.data = model.matrix( V0~ 0 + ., data = cbind(train.data.labels,primary.train.data))
    # test.data = model.matrix( V0~ 0 + ., data = cbind(test.data.labels,primary.test.data))
    #########################################################################################################################################
    
    
    # rm(primary.train.data)
    # rm(primary.test.data)
    # rm(primary.train.data.labels)
    # rm(primary.test.data.labels)
    
    
    
    #########################################################################################################################################
    ##########################################    start of Lasso (feature selection)    #####################################################
    #########################################################################################################################################
    # lasso.lambda.object = cv.glmnet(x = train.data, y = as.integer(as.matrix(train.data.labels)), lambda = (2^seq(-10,10,by = 0.02)))
    # lasso.lambda.object$lambda.min
    # 
    # # Note alpha=1 for lasso only and can blend with ridge penalty down to
    # # alpha=0 ridge only.
    # lasso.fit = glmnet(x = train.data, y = as.factor(as.matrix(train.data.labels)), alpha = 1, family="binomial", lambda = lasso.lambda.object$lambda)
    # 
    # 
    # selected.feature = coef(lasso.fit, s = lasso.lambda.object$lambda.min)
    # selected.feature.indice = (which(selected.feature != 0,arr.ind = FALSE) - 1)
    # selected.feature.indice = selected.feature.indice[-1]
    # 
    # selected.feature.train.data = train.data
    # selected.feature.train.data = selected.feature.train.data[,selected.feature.indice]
    # 
    # selected.feature.test.data = test.data
    # selected.feature.test.data = selected.feature.test.data[,selected.feature.indice]
    # 
    # 
    # gen.train.data = model.matrix( V0~ 0 + ., data = cbind(train.data.labels,selected.feature.train.data))
    # gen.train.data.labels = train.data.labels
    # gen.test.data = model.matrix( V0~ 0 + ., data = cbind(test.data.labels,selected.feature.test.data))
    # gen.test.data.labels = test.data.labels
    # 
    # 
    # gen.whole.train.Data = cbind(gen.train.data.labels,gen.train.data)
    # gen.whole.test.Data = cbind(gen.test.data.labels,gen.test.data)
    
    ###########################################################################################################
    ####################################    End of Lasso (feature selection)    ###############################
    ###########################################################################################################
    
    
    # results[i,1] = calculate.logistic(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,2] = calculate.lda(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,3] = calculate.qda(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,4] = calculate.knn(gen.train.data, gen.test.data, gen.train.data.labels, gen.whole.test.Data)
    # results[i,5] = calculate.svm.linear(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,6] = calculate.svm.polynomial(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,7] = calculate.svm.sigmoid(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,8] = calculate.svm.radial(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,9] = calculate.svm.anovadot(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,10] = calculate.random.forest(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,11] = calculate.random.forest(cbind(train.data.labels, train.data), cbind(test.data.labels, test.data))
    # results[i,12] = calculate.Boosting(gen.whole.train.Data, gen.whole.test.Data)
    # results[i,13] = calculate.Boosting(cbind(train.data.labels, train.data), cbind(test.data.labels, test.data))
    
    # results[i,13] = calculate.Boosting(primary.train.data, primary.test.data)
    
     
    
    # str(primary.train.data)
    train = as(primary.train.data, "dgCMatrix")
    test = as(primary.test.data, "dgCMatrix")
    # str(train)
    # train = sparse.model.matrix(V0~.-1,data = tt)
    
    p.train = train[train[,1] == 1,]
    n.train = train[train[,1] == 0,]
    head(p.train)[,1:10]
    head(n.train)[,1:10]
    dim(train)
    dim(p.train)
    dim(n.train)
    
    p.test = test[test[,1] == 1,]
    n.test = test[test[,1] == 0,]
    head(p.test)[,1:10]
    head(n.test)[,1:10]
    dim(test)
    dim(p.test)
    dim(n.test)
    
    temp1 = apply(p.test, 1, sum)
    temp2 = apply(n.test, 1, sum)
    table(temp1)
    # pairs()
    png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "Dencity.png", sep = ""))
    hist(temp1, col="blue", xlab="Frequency", ylab = "Dencity", main="Number of STRs per P/N Sampels")
    hist(temp2, col="red",  add=TRUE)
    dev.off()
    
    # temp.to.show1 = as.data.frame(temp1)
    # temp.to.show2 = as.data.frame(temp2)
    # temp.to.show1$veg = "po"
    # temp.to.show2$veg = "no"
    # temp.to.show = rbind(temp.to.show1, temp.to.show2)
    # ggplot(temp.to.show1, aes(length, fill = veg)) + geom_density(alpha = 0.2)
    
    
    
    
    xgboost.fit = xgboost(data = train[,-1], label = train[,1], max.depth = 2, eta = 1, nrounds = 6000,
                          nthread = 4, objective = "binary:logistic")
    
    # predict
    pred = predict(xgboost.fit, test[,-1])
    
    prediction = as.numeric(pred > 0.5)    
    
    err = mean(as.numeric(pred > 0.5) != test[,1])
    print(paste("test-error=", err))
    confusionMatrix(as.factor(prediction), as.factor(test[,1]))    
    
    
    summary(xgboost.fit)
    
    importance_matrix = xgboost.fit(colnames(train), model=xgboost.fit)[0:20]
    
    ###################### add feature  ###############
    new.train = train
    new.test = test
    new.train = cbind(new.train, apply(train, 1, sum))
    new.test = cbind(new.test, apply(test, 1, sum))

    # new.train = cbind(train[,1], apply(train, 1, sum))
    # new.test = cbind(test[,1], apply(test, 1, sum))
    
    xgboost.fit = xgboost(data = as.matrix(new.train[,-1]), label = as.matrix(new.train[,1]), max.depth = 1, eta = 1, nrounds = 1000,
                          nthread = 4, objective = "binary:logistic")
    
    # predict
    pred = predict(xgboost.fit, as.matrix(new.test[,2]))
    
    prediction = as.numeric(pred > 0.5)    
    
    err = mean(as.numeric(pred > 0.5) != as.matrix(new.test[,1]))
    print(paste("test-error=", err))
    confusionMatrix(as.factor(prediction), as.factor(new.test[,1]))    
    
    
    
    
    
    
    train.data = model.matrix( V0~ 0 + ., data = cbind(trai,train))
    test.data = model.matrix( V0~ 0 + ., data = cbind(test.data.labels,test))
    colnames(train)[1] = "V0"
    colnames(test)[1] = "V0"
    head(train)[,1:4]
    head(test)[,1:4]
    aa = calculate.random.forest(as.matrix(train),as.matrix(test),1)
    gen.whole.train.Data = as.data.frame(train)
    gen.whole.test.Data = as.data.frame(test)
    randomForest.fit = randomForest(V0~. ,data = rbind(gen.whole.train.Data, gen.whole.test.Data) 
                                    ,subset = row.names(gen.whole.train.Data) 
                                    ,mtry = as.integer(sqrt(dim(gen.whole.train.Data)[2]-1))
                                    ,importance =TRUE)
    
    # yhat.randomForest = predict (randomForest.fit ,newdata = gen.whole.test.Data)
    # importance(randomForest.fit)
    png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "Feature Importance top fold "
                                                                   , fold.number,".png", sep = ""))
    varImpPlot(randomForest.fit, n.var = 20, main = "Feature Importance")
    dev.off()
    
    variable = row.names(randomForest.fit$importance)
    importance = randomForest.fit$importance[,1]
    png(height=140, width=150, units = "mm", res = 300, file=paste(saveAddress, "Feature Importance total fold "
                                                                   , fold.number,".png", sep = ""))
    ggplot(randomForest.fit$importance, aes(x=reorder(variable,importance), y=importance,fill=importance))+ 
        geom_bar(stat="identity", position="dodge")+ coord_flip()+
        ylab("Feature Importance")+
        xlab("")+
        ggtitle("Information Value Summary")+
        guides(fill=F)+
        scale_fill_gradient(low="red", high="blue")
    dev.off()
    
    randomForest.predict = predict(randomForest.fit, newdata = gen.whole.test.Data)
    error.continues = mean((randomForest.predict - gen.whole.test.Data$V0)^2)
    coefficient = importance(randomForest.fit)
    misClassError = misClassError(randomForest.predict > 5, gen.whole.test.Data$V0 > 5)
    confusion.matrix = table(randomForest.predict > 5, gen.whole.test.Data$V0 > 5)
    sensitivity = sensitivity(randomForest.predict > 5, gen.whole.test.Data$V0 > 5)
    specificity = specificity(randomForest.predict > 5, gen.whole.test.Data$V0 > 5)
    
    result = list(randomForest.fit, randomForest.predict, coefficient, error.continues, misClassError, sensitivity, specificity, confusion.matrix)
    
    
    
    
    
    summary(xgboost.fit)
    
    importance_matrix = xgboost.fit(colnames(train), model=xgboost.fit)[0:20]
    
    # $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    gen.whole.train.Data = cbind(train.data.labels, train.data)
    gen.whole.test.Data = cbind(test.data.labels, test.data)
    
    powers = seq(-5, -0.5, by = 0.6)
    lambdas = 10 ^ powers
    mse = rep(0, length(lambdas))
    
    for (i in 1:length(mse)) 
    {
        boost.hitters = gbm(V0~.,data = gen.whole.train.Data, distribution = "multinomial"
                            ,n.trees =1000 , interaction.depth = 1 ,shrinkage = lambdas[i])
        summary(boost.hitters)
        yhat.boost = predict(boost.hitters, gen.whole.test.Data, n.trees = 1000)
        yhat.boost = apply(yhat.boost, 1, which.max) - 1
        mse[i] = misClassError(ifelse(yhat.boost[,2,1] > 0.006, 1, 0), as.numeric(gen.whole.test.Data$V0)-1)
        confusionMatrix(ifelse(yhat.boost[,2,1] > 0.005, 1, 0), as.numeric(gen.whole.test.Data$V0)-1)
    }
    error.min = which.min(mse)
    mse[error.min]
    
    
    #***********************************************************************************************************    
    #***********************************************************************************************************    
    #********************************************       caret           ****************************************    
    #***********************************************************************************************************    
    #***********************************************************************************************************    
    
    printSpMatrix(train[1:10,1:20], col.names = TRUE)
    s.train = sparse.model.matrix()
    
    
    fitControl = trainControl(method = "repeatedcv", number = 10, repeats = 1)
    
    
    random.fit = train(V0~., data = myData, method='rf', tuneLength=2, trControl = fitControl)
    
    
    lasso.fit = train(V0~., data = myData, method = "glmnet", lambda= 0,
                      tuneGrid = expand.grid(alpha = 1,  lambda = 0), trControl = fitControl)

    lasso.fit = train(V0~., data = myData, method = "glmnet", lambda= 0,
                      tuneGrid = expand.grid(alpha = 1,  lambda = 0), trControl = fitControl)


    lm.fit = train(V0~., data = myData, method = "lasso", trControl = fitControl)







#***********************************************************************************************************    
#***********************************************************************************************************    




# $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$4


print(paste(i, " loop finished          ", Sys.time(), sep = "") )
print(results[i,])
}

SE = function(x) sd(x)/sqrt(length(x))

for (i in 1:13) 
{
    pe = mean(results[,i])
    z = 1.92 * SE(results[,i])
    print(paste("i = ", i , " -> confidence interval = ", pe, " +- ", z, sep = ""))
}











randomForest.fit = randomForest(myDataLabels~. ,data = rbind(gen.whole.train.Data, gen.whole.test.Data) 
                                ,subset = row.names(gen.whole.train.Data) ,
                                importance =TRUE)

yhat.randomForest = predict (randomForest.fit ,newdata = gen.whole.test.Data)

