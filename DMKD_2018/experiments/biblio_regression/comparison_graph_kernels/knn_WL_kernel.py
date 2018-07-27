import os
import sys
import math
import numpy as np
from sklearn.metrics import mean_squared_error
from math import sqrt

matrix_file = sys.argv[1]
num_train = int(sys.argv[2])
num_test = int(sys.argv[3])
K = int(sys.argv[4])
train_labels_file = sys.argv[5]
test_labels_file = sys.argv[6]

labels_train = []
labels_test = []

with open(train_labels_file) as f:
    for line in f:
        labels_train.append(float(line.strip()))

with open(test_labels_file) as f:
    for line in f:
        labels_test.append(float(line.strip()))

labels_train = np.array(labels_train)
labels_test = np.array(labels_test)

predicted = []
target = []

count = 0
with open(matrix_file) as f:
    for line in f:
        count = count + 1
        if (count <= num_train):
            continue
        values = np.array(line.strip().split())[0:num_train]
        nn = list(values.argsort()[-K:])
        to_be_appended = [i for i, x in enumerate(values) if x == nn[0]]
        nn.extend(x for x in to_be_appended if x not in nn)
        print(nn)
        print(labels_train[np.array(nn)])
        pred = np.mean(labels_train[np.array(nn)])
        true = labels_test[count - num_train - 1]
        print(str(true) + " " + str(pred))
        target.append(true)
        predicted.append(pred)
print('RMSE: ' + str(sqrt(mean_squared_error(target, predicted))))


