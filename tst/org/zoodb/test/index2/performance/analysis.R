
data <- read.table('performanceTest.csv', sep=",",header=T,quote="")

library(ggplot2)
#plot 
ggplot(data=data, aes(x=numElements, y=index, colour="blue")) + geom_line() + geom_point()+ylab("time (ms)")

#for plotting
data2 <- data
data3 <- data
data2$query <- "regular"
data3$query <- "indexed"
data2$queryTime <- data$regularQuery
data3$queryTime <- data$indexedQuery
data4 <- rbind(data2,data3)

ggplot(data=data4, aes(x=numElements, y=queryTime, group=query, colour=query)) + geom_line() + geom_point() +ylab("time (ms)")
ggplot(data=data4[data4$numElements<5000,], aes(x=numElements, y=queryTime, group=query, colour=query)) 
            + geom_line() + geom_point()+ylab("time (ms)")