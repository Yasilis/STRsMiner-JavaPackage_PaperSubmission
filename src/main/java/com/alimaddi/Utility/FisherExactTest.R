# Title     : TODO
# Objective : TODO
# Created by: Ali-Laptop
# Created on: 12/6/2020


rm(list=ls())
#library()

#$ : Case	 = 	[2625, 5200, 0, 2463, 5362, 0]
#$ : Control 1	 = 	[4439, 6010, 0, , 0]	 | 	P-value(1) = 0 					 p-value(2) =  0
#$ : Control 2	 = 	[3648, 6864, 0, , 0]	 | 	P-value(1) = 0.003309 					 p-value(2) =  0.002508
#$ : Control 3	 = 	[3402, 6722, 1, , 1]	 | 	P-value(1) = 0.0126746 					 p-value(2) =  0.0069049
#$ : Control 4	 = 	[3863, 6730, 0, , 0]	 | 	P-value(1) = 0.0000027 					 p-value(2) =  0.0000002
#$ : Control 5	 = 	[4210, 6428, 1, , 1]	 | 	P-value(1) = 0 					 p-value(2) =  0
#$ : Control 6	 = 	[4146, 4928, 0, , 0]	 | 	P-value(1) = 0 					 p-value(2) =  0
#$ : Control 7	 = 	[3305, 6770, 1, , 1]	 | 	P-value(1) = 0.0073817 					 p-value(2) =  0.0126549
#$ : Control 8	 = 	[3803, 6681, 0, , 0]	 | 	P-value(1) = 0.0000082 					 p-value(2) =  0.0000007
#$ : Control 9	 = 	[4022, 5906, 0, , 0]	 | 	P-value(1) = 0 					 p-value(2) =  0
#$ : Control 10	 = 	[4489, 6765, 0, , 0]	 | 	P-value(1) = 0 					 p-value(2) =  0
#$ : Total ctrl	 = 	[3932.7, 6380.4, 0.3, , 0.3]	 | 	P-value(1) = 0 	 p-value(2) =  0

i <- 1
print("==============================================================================================")
print("======================================   16:120   ============================================")
print("==============================================================================================")
#16:120 (1)
print("---------------------------------------- Method 1 --------------------------------------------")
print("Result 1")
fisher.test(rbind(c(2712, 6506),c(7715, 12119)), alternative="less")$p.value
print("Result 2")
fisher.test(rbind(c(2712, 6506),c(9542, 12755)), alternative="less")$p.value
print("Result 3")
fisher.test(rbind(c(2712, 6506),c(7336, 13877)), alternative="less")$p.value
print("Result 4")
fisher.test(rbind(c(2712, 6506),c(11611, 19357)), alternative="less")$p.value
print("Result 5")
fisher.test(rbind(c(2712, 6506),c(14125, 11960)), alternative="less")$p.value
print("Result 6")
fisher.test(rbind(c(2712, 6506),c(10092, 9854)), alternative="less")$p.value
print("Result 7")
fisher.test(rbind(c(2712, 6506),c(9430, 12579)), alternative="less")$p.value
print("Result 8")
fisher.test(rbind(c(2712, 6506),c(10371, 7999)), alternative="less")$p.value
print("Result 9")
fisher.test(rbind(c(2712, 6506),c(8226, 8157)), alternative="less")$p.value
print("Result 10")
fisher.test(rbind(c(2712, 6506),c(11601, 17514)), alternative="less")$p.value
print("Result Total")
fisher.test(rbind(c(2712, 6506),c(10005, 12617)), alternative="less")$p.value
#16:120 (1)
print("---------------------------------------- Method 2 --------------------------------------------")
print("Result 1")
fisher.test(rbind(c(2547, 6671),c(7309, 12525)), alternative="less")$p.value
print("Result 2")
fisher.test(rbind(c(2547, 6671),c(9022, 13275)), alternative="less")$p.value
print("Result 3")
fisher.test(rbind(c(2547, 6671),c(6982, 14231)), alternative="less")$p.value
print("Result 4")
fisher.test(rbind(c(2547, 6671),c(11169, 19799)), alternative="less")$p.value
print("Result 5")
fisher.test(rbind(c(2547, 6671),c(13189, 12896)), alternative="less")$p.value
print("Result 6")
fisher.test(rbind(c(2547, 6671),c(9502, 10444)), alternative="less")$p.value
print("Result 7")
fisher.test(rbind(c(2547, 6671),c(9122, 12887)), alternative="less")$p.value
print("Result 8")
fisher.test(rbind(c(2547, 6671),c(10061, 8309)), alternative="less")$p.value
print("Result 9")
fisher.test(rbind(c(2547, 6671),c(7806, 8577)), alternative="less")$p.value
print("Result 10")
fisher.test(rbind(c(2547, 6671),c(10974, 18141)), alternative="less")$p.value
print("Result Total")
fisher.test(rbind(c(2547, 6671),c(9514, 13108)), alternative="less")$p.value
print("==============================================================================================")


print("==============================================================================================")
print("======================================   10:15   ======= =====================================")
print("==============================================================================================")
#10:15 (1)
print("---------------------------------------- Method 1 --------------------------------------------")
print("Result 1")
fisher.test(rbind(c(3627, 10682),c(18549, 26582)), alternative="less")$p.value
print("Result 2")
fisher.test(rbind(c(3627, 10682),c(14904, 21471)), alternative="less")$p.value
print("Result 3")
fisher.test(rbind(c(3627, 10682),c(21702, 22142)), alternative="less")$p.value
print("Result 4")
fisher.test(rbind(c(3627, 10682),c(16345, 15103)), alternative="less")$p.value
print("Result 5")
fisher.test(rbind(c(3627, 10682),c(20644, 19977)), alternative="less")$p.value
print("Result 6")
fisher.test(rbind(c(3627, 10682),c(17611, 27594)), alternative="less")$p.value
print("Result 7")
fisher.test(rbind(c(3627, 10682),c(18056, 17720)), alternative="less")$p.value
print("Result 8")
fisher.test(rbind(c(3627, 10682),c(15826, 25744)), alternative="less")$p.value
print("Result 9")
fisher.test(rbind(c(3627, 10682),c(15247, 24008)), alternative="less")$p.value
print("Result 10")
fisher.test(rbind(c(3627, 10682),c(15917, 19476)), alternative="less")$p.value
print("Result Total")
fisher.test(rbind(c(3627, 10682),c(17480, 21982)), alternative="less")$p.value

print("---------------------------------------- Method 2 --------------------------------------------")
#10:15 (2)
print("Result 1")
fisher.test(rbind(c(3437, 10872),c(17448, 27683)), alternative="less")$p.value
print("Result 2")
fisher.test(rbind(c(3437, 10872),c(14258, 22117)), alternative="less")$p.value
print("Result 3")
fisher.test(rbind(c(3437, 10872),c(20877, 22967)), alternative="less")$p.value
print("Result 4")
fisher.test(rbind(c(3437, 10872),c(15637, 15811)), alternative="less")$p.value
print("Result 5")
fisher.test(rbind(c(3437, 10872),c(19799, 20822)), alternative="less")$p.value
print("Result 6")
fisher.test(rbind(c(3437, 10872),c(16850, 28355)), alternative="less")$p.value
print("Result 7")
fisher.test(rbind(c(3437, 10872),c(17043, 18733)), alternative="less")$p.value
print("Result 8")
fisher.test(rbind(c(3437, 10872),c(15120, 26450)), alternative="less")$p.value
print("Result 9")
fisher.test(rbind(c(3437, 10872),c(14588, 24667)), alternative="less")$p.value
print("Result 10")
fisher.test(rbind(c(3437, 10872),c(15079, 20314)), alternative="less")$p.value
print("Result Total")
fisher.test(rbind(c(3437, 10872),c(16670, 22792)), alternative="less")$p.value
print("==============================================================================================")


print("==============================================================================================")
print("=======================================   7:9   ==============================================")
print("==============================================================================================")
#7:9 (1)
print("---------------------------------------- Method 1 --------------------------------------------")
print("Result 1")
fisher.test(rbind(c(1397, 3779),c(5447, 12967)), alternative="less")$p.value
print("Result 2")
fisher.test(rbind(c(1397, 3779),c(2602, 3941)), alternative="less")$p.value
print("Result 3")
fisher.test(rbind(c(1397, 3779),c(5312, 5920)), alternative="less")$p.value
print("Result 4")
fisher.test(rbind(c(1397, 3779),c(3244, 4684)), alternative="less")$p.value
print("Result 5")
fisher.test(rbind(c(1397, 3779),c(2748, 5030)), alternative="less")$p.value
print("Result 6")
fisher.test(rbind(c(1397, 3779),c(3810, 4814)), alternative="less")$p.value
print("Result 7")
fisher.test(rbind(c(1397, 3779),c(3701, 2927)), alternative="less")$p.value
print("Result 8")
fisher.test(rbind(c(1397, 3779),c(3684, 5317)), alternative="less")$p.value
print("Result 9")
fisher.test(rbind(c(1397, 3779),c(4696, 4918)), alternative="less")$p.value
print("Result 10")
fisher.test(rbind(c(1397, 3779),c(3823, 3888)), alternative="less")$p.value
print("Result Total")
fisher.test(rbind(c(1397, 3779),c(3907, 5441)), alternative="less")$p.value

print("---------------------------------------- Method 2 --------------------------------------------")
#7:9 (2)
print("Result 1")
fisher.test(rbind(c(1349, 3827),c(5034, 13380)), alternative="less")$p.value
print("Result 2")
fisher.test(rbind(c(1349, 3827),c(2422, 4121)), alternative="less")$p.value
print("Result 3")
fisher.test(rbind(c(1349, 3827),c(5189, 6043)), alternative="less")$p.value
print("Result 4")
fisher.test(rbind(c(1349, 3827),c(3118, 4810)), alternative="less")$p.value
print("Result 5")
fisher.test(rbind(c(1349, 3827),c(2434, 5344)), alternative="less")$p.value
print("Result 6")
fisher.test(rbind(c(1349, 3827),c(3591, 5033)), alternative="less")$p.value
print("Result 7")
fisher.test(rbind(c(1349, 3827),c(3566, 3062)), alternative="less")$p.value
print("Result 8")
fisher.test(rbind(c(1349, 3827),c(3535, 5466)), alternative="less")$p.value
print("Result 9")
fisher.test(rbind(c(1349, 3827),c(4474, 5140)), alternative="less")$p.value
print("Result 10")
fisher.test(rbind(c(1349, 3827),c(3664, 4047)), alternative="less")$p.value
print("Result Total")
fisher.test(rbind(c(1349, 3827),c(3703, 5645)), alternative="less")$p.value
print("==============================================================================================")


print("==============================================================================================")
print("=======================================   1:6   ==============================================")
print("==============================================================================================")
#1:6 (1)
print("---------------------------------------- Method 1 --------------------------------------------")
print("Result 1")
fisher.test(rbind(c(27999, 260406),c(501256, 633743)), alternative="less")$p.value
print("Result 2")
fisher.test(rbind(c(27999, 260406),c(519096, 725608)), alternative="less")$p.value
print("Result 3")
fisher.test(rbind(c(27999, 260406),c(557048, 733004)), alternative="less")$p.value
print("Result 4")
fisher.test(rbind(c(27999, 260406),c(516512, 640416)), alternative="less")$p.value
print("Result 5")
fisher.test(rbind(c(27999, 260406),c(543449, 663753)), alternative="less")$p.value
print("Result 6")
fisher.test(rbind(c(27999, 260406),c(547184, 705538)), alternative="less")$p.value
print("Result 7")
fisher.test(rbind(c(27999, 260406),c(541038, 699813)), alternative="less")$p.value
print("Result 8")
fisher.test(rbind(c(27999, 260406),c(518569, 690588)), alternative="less")$p.value
print("Result 9")
fisher.test(rbind(c(27999, 260406),c(525393, 666866)), alternative="less")$p.value
print("Result 10")
fisher.test(rbind(c(27999, 260406),c(520976, 648667)), alternative="less")$p.value
print("Result Total")
fisher.test(rbind(c(27999, 260406),c(529052, 680800)), alternative="less")$p.value

print("---------------------------------------- Method 2 --------------------------------------------")
#1:6 (2)
print("Result 1")
fisher.test(rbind(c(24102, 264303),c(477161, 657838)), alternative="less")$p.value
print("Result 2")
fisher.test(rbind(c(24102, 264303),c(491509, 753195)), alternative="less")$p.value
print("Result 3")
fisher.test(rbind(c(24102, 264303),c(528765, 761287)), alternative="less")$p.value
print("Result 4")
fisher.test(rbind(c(24102, 264303),c(493044, 663884)), alternative="less")$p.value
print("Result 5")
fisher.test(rbind(c(24102, 264303),c(518312, 688890)), alternative="less")$p.value
print("Result 6")
fisher.test(rbind(c(24102, 264303),c(517638, 735084)), alternative="less")$p.value
print("Result 7")
fisher.test(rbind(c(24102, 264303),c(516663, 724188)), alternative="less")$p.value
print("Result 8")
fisher.test(rbind(c(24102, 264303),c(490712, 718445)), alternative="less")$p.value
print("Result 9")
fisher.test(rbind(c(24102, 264303),c(500134, 692125)), alternative="less")$p.value
print("Result 10")
fisher.test(rbind(c(24102, 264303),c(498327, 671316)), alternative="less")$p.value
print("Result Total")
fisher.test(rbind(c(24102, 264303),c(503226, 706625)), alternative="less")$p.value
print("==============================================================================================")