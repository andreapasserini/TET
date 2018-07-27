import sys
import numpy as np

gold_ranking = sys.argv[1]
pred_ranking = sys.argv[2]

nn_gold = []
nn_pred = []

with open(gold_ranking) as f:
    for line in f:
        if "DISTANCES" in line:
            values = line.strip().split(":")[1]
            values = values.strip().split(",")
            values = [v.strip() for v in values]
            nn_gold.append(np.argsort(values[:len(values)-1]))

with open(pred_ranking) as f:
    for line in f:
        if "DISTANCES" in line:
            values = line.strip().split(":")[1]
            values = values.strip().split(",")
            values = [v.strip() for v in values]
            nn_pred.append(np.argsort(values[:len(values)-1]))

for i in range(len(nn_pred)):
    nn_p = nn_pred[i]
    nn_g = nn_gold[i]
    s = ''
    for j in range(3):
        s += str(np.where(nn_g==nn_p[j])[0][0]+1) + ","
    print(s)





