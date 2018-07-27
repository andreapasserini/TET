import sys
import numpy as np

gold_ranking = sys.argv[1]
pred_ranking = sys.argv[2]

min_gold = []
min_pred = []

with open(gold_ranking) as f:
    for line in f:
        if "DISTANCES" in line:
            values = line.strip().split(":")[1]
            values = values.strip().split(",")
            values = [v.strip() for v in values]
            min_gold.append(np.amin(np.array(values[:len(values)-1])))

with open(pred_ranking) as f:
    for line in f:
        if "DISTANCES" in line:
            values = line.strip().split(":")[1]
            values = values.strip().split(",")
            values = [v.strip() for v in values]
            min_pred.append(np.amin(np.array(values[:len(values)-1])))

s = 0
for i in range(len(min_pred)):
    min_p = min_pred[i]
    min_g = min_gold[i]
    s += (min_p - min_g)/min_g
print(s/len(min_pred))



